package com.pixelcrater.Diaro.storage.dropbox;

import static com.pixelcrater.Diaro.config.GlobalConstants.BATCH_DELETE_SIZE;
import static com.pixelcrater.Diaro.config.GlobalConstants.DROPBOX_KEY;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import androidx.preference.PreferenceManager;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.InvalidAccessTokenException;
import com.dropbox.core.NetworkIOException;
import com.dropbox.core.RetryException;
import com.dropbox.core.android.Auth;
import com.dropbox.core.http.OkHttp3Requestor;
import com.dropbox.core.http.StandardHttpRequestor;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.CommitInfo;
import com.dropbox.core.v2.files.DeleteArg;
import com.dropbox.core.v2.files.DeleteBatchLaunch;
import com.dropbox.core.v2.files.DeletedMetadata;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.UploadSessionCursor;
import com.dropbox.core.v2.files.UploadSessionFinishErrorException;
import com.dropbox.core.v2.files.WriteMode;
import com.dropbox.core.v2.users.FullAccount;
import com.dropbox.core.v2.users.SpaceUsage;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.Static;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class DropboxAccountManager {

    public static final String PREF_DROPBOX_TOKEN = "dropbox.token";

    // we do not use it yet!
    public static final String PREF_DROPBOX_UID_V2 = "dropbox.uid";
    public static final String PREF_DROPBOX_UID_V1 = "dropbox.uid_v1";

    public static final String PREF_DROPBOX_EMAIL = "dropbox.email";
    //  Auth.getOAuth2Token() might still give last token onresume, so be careful.
    public static final String PREF_DROPBOX_LAST_REVOKED_TOKEN = "dropbox.revoked_token";

    private static DbxClientV2 sDbxClient;

    private static DbxClientV2 sDbxLongpollClient;

    public static boolean isLoggedIn(Context context) {
        return getToken(context) != null;
    }

    public static String getToken(Context context) {
        //AppLog.e("token is " +  PreferenceManager.getDefaultSharedPreferences(context) .getString(PREF_DROPBOX_TOKEN, null));
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_DROPBOX_TOKEN, null);

    }

    public static DbxClientV2 getDropboxClient(Context context) {
        if (sDbxClient == null) {
            DbxRequestConfig requestConfig = DbxRequestConfig.newBuilder("Diaro").withHttpRequestor(new OkHttp3Requestor(OkHttp3Requestor.defaultOkHttpClient())).withAutoRetryEnabled(5).build();

            if (getToken(context) == null || context == null) {
                return sDbxClient;
            }
            sDbxClient = new DbxClientV2(requestConfig, getToken(context));
        }
        return sDbxClient;
    }

    public static DbxClientV2 getDropboxLongpollClient(Context context) {
        if (sDbxLongpollClient == null) {

            StandardHttpRequestor.Config longpollConfig = StandardHttpRequestor.Config.DEFAULT_INSTANCE.copy().withReadTimeout(5, TimeUnit.MINUTES).build();
            StandardHttpRequestor requestor = new StandardHttpRequestor(longpollConfig);
            DbxRequestConfig requestConfig = DbxRequestConfig.newBuilder("Diaro-Longpoll").withHttpRequestor(requestor).build();
            sDbxLongpollClient = new DbxClientV2(requestConfig, getToken(context));
        }
        return sDbxLongpollClient;
    }

    public static void link(Activity activity) {
        Auth.startOAuth2Authentication(activity, DROPBOX_KEY);
    }

    public static void unlink(Context context) {
        try {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString(PREF_DROPBOX_LAST_REVOKED_TOKEN, getToken(context)).apply();
            getDropboxClient(context).auth().tokenRevoke();

            sDbxClient = null;
            sDbxLongpollClient = null;

        } catch (DbxException e) {
            AppLog.w("Error revoking Dropbox token");
        }

        DropboxLocalHelper.clearLocalDropboxData(context);
    }

    public static void deltaCheck(List<FileMetadata> filesToDownload, List<Metadata> filesToDelete) {

        long time = System.currentTimeMillis();

        DbxClientV2 dbxClient = DropboxAccountManager.getDropboxClient(MyApp.getInstance());
        if (dbxClient == null) {
            return;
        }

        List<Metadata> folderList = new ArrayList<>();
        String cursor = DropboxLocalHelper.getLatestCursor();
        String dbxFolderPath = "";
        try {
            if (cursor == null) {
                AppLog.d("Previous cursor doesn't exist");
                ListFolderResult listFolderResult = dbxClient.files().listFolderBuilder(dbxFolderPath).withIncludeDeleted(false).withRecursive(true).start();

                if (listFolderResult != null) {
                    cursor = listFolderResult.getCursor();
                    folderList.addAll(listFolderResult.getEntries());
                    while (listFolderResult != null && listFolderResult.getHasMore()) {
                        listFolderResult = dbxClient.files().listFolderContinue(listFolderResult.getCursor());
                        if (listFolderResult != null) {
                            cursor = listFolderResult.getCursor();
                            folderList.addAll(listFolderResult.getEntries());
                        }
                    }
                }
            } else {
                AppLog.d("Using previous cursor");
                ListFolderResult listFolderResult = dbxClient.files().listFolderContinue(cursor);
                if (listFolderResult != null) {
                    cursor = listFolderResult.getCursor();
                    folderList.addAll(listFolderResult.getEntries());
                    while (listFolderResult != null && listFolderResult.getHasMore()) {
                        listFolderResult = dbxClient.files().listFolderContinue(listFolderResult.getCursor());
                        if (listFolderResult != null) {
                            cursor = listFolderResult.getCursor();
                            folderList.addAll(listFolderResult.getEntries());
                        }
                    }
                }
            }

            int changeCount = folderList.size();
            AppLog.d(String.format(Locale.US, "deltaSync: changes=%d", changeCount));
            if (changeCount > 0) {
                for (Metadata metadata : folderList) {
                    if (metadata instanceof FileMetadata) {
                        filesToDownload.add((FileMetadata) metadata);
                    } else if (metadata instanceof DeletedMetadata) {
                        filesToDelete.add(metadata);
                    } else {
                        AppLog.e(String.format("Unrecognized metadata type: %s", metadata.getPathLower()));
                    }
                }
            }

            //Only updated the persisted cursor if everything went through without error
            DropboxLocalHelper.setLatestCursor(cursor);

        } catch (Exception e) {
            AppLog.w(String.format("Error checking Dropbox delta : %s", e.getMessage()));
        } finally {
        }

        AppLog.i("DeltaCheck took " + (System.currentTimeMillis() - time));
    }

    public static void batchDelete(List<String> deletionQueue) throws DbxException {
        AppLog.d(String.format(Locale.US, "Deletion queue size: %d", deletionQueue == null ? 0 : deletionQueue.size()));
        int batch_size = BATCH_DELETE_SIZE;
        List<DeleteArg> deleteArgs = new ArrayList<>();
        if (deletionQueue != null && !deletionQueue.isEmpty()) {
            for (String dbxPath : deletionQueue) {
                if (Pattern.matches("(/(.|[\\r\\n])*)|(ns:[0-9]+(/.*)?)|(id:.*)", dbxPath)) {
                    deleteArgs.add(new DeleteArg(dbxPath));
                }
            }
        }

        List<List<DeleteArg>> batches = new ArrayList<>();
        for (int i = 0; i < deleteArgs.size(); i += batch_size) {
            batches.add(deleteArgs.subList(i, Math.min(i + batch_size, deleteArgs.size())));
        }

        if (!deleteArgs.isEmpty()) {
            AppLog.e(String.format(Locale.US, "Deleting total %d files from Dropbox", deleteArgs.size()));
            DbxClientV2 dbxClient = DropboxAccountManager.getDropboxClient(MyApp.getInstance());
            for (int i = 0; i < batches.size(); i++) {
                List<DeleteArg> tmpDeleteArgs = batches.get(i);
                AppLog.e("Deleting batch " + i + " with " + tmpDeleteArgs.size() + "files");
                DeleteBatchLaunch deleteBatchLaunchResult = dbxClient.files().deleteBatch(tmpDeleteArgs);
                while (!dbxClient.files().deleteBatchCheck(deleteBatchLaunchResult.getAsyncJobIdValue()).isComplete()) {
                    Static.makePause(100);
                }
                // DeleteBatchJobStatus deleteBatchJobStatus = dbxClient.files().deleteBatchCheck(deleteBatchLaunchResult.getAsyncJobIdValue());
            }
            // clear the local deletion queue
            // TODO : imporive this based on DeleteBatchJobStatus result ( just removed the actual deleted entries from deletion queues)
            DropboxLocalHelper.clearDeletionQueue();
        } else
            DropboxLocalHelper.clearDeletionQueue();
    }

    public static final long CHUNKED_UPLOAD_CHUNK_SIZE = 2L << 20;
    private static final int CHUNKED_UPLOAD_MAX_ATTEMPTS = 5;

    public interface ProgressCallback {
        void progressUpdate(long uploaded, long size);
    }

    public static FileMetadata uploadFile(File localFile, String dropboxFilePath) throws Exception {
        FileInputStream in = new FileInputStream(localFile);
        DbxClientV2 dbxClient = DropboxAccountManager.getDropboxClient(MyApp.getInstance());
        FileMetadata fileMetadata = dbxClient.files().uploadBuilder(dropboxFilePath).withMode(WriteMode.OVERWRITE).uploadAndFinish(in);
        in.close();
        return fileMetadata;
    }

    /**
     * Uploads a file in chunks using multiple requests. This approach is preferred for larger files
     * since it allows for more efficient processing of the file contents on the server side and
     * also allows partial uploads to be retried (e.g. network connection problem will not cause you
     * to re-upload all the bytes).
     */
    public static void chunkedUploadFile(File localFile, String dropboxPath, ProgressCallback progressCallback) throws Exception {

        long size = localFile.length();
        long uploaded = 0L;
        DbxException thrown = null;

        DbxClientV2 dbxClient = DropboxAccountManager.getDropboxClient(MyApp.getInstance());

        // Chunked uploads have 3 phases, each of which can accept uploaded bytes:
        //
        //    (1)  Start: initiate the upload and get an upload session ID
        //    (2) Append: upload chunks of the file to append to our session
        //    (3) Finish: commit the upload and close the session
        //
        // We track how many bytes we uploaded to determine which phase we should be in.
        String sessionId = null;
        for (int i = 0; i < CHUNKED_UPLOAD_MAX_ATTEMPTS; ++i) {
            if (i > 0) {
                AppLog.d("Retrying chunked upload " + (i + 1));
                //AppLog.d("Retrying chunked upload (%d / %d attempts)\n", i + 1, CHUNKED_UPLOAD_MAX_ATTEMPTS);
            }

            try (InputStream in = new FileInputStream(localFile)) {
                // if this is a retry, make sure seek to the correct offset
                in.skip(uploaded);

                // (1) Start
                if (sessionId == null) {
                    sessionId = dbxClient.files().uploadSessionStart().uploadAndFinish(in, CHUNKED_UPLOAD_CHUNK_SIZE).getSessionId();
                    uploaded += CHUNKED_UPLOAD_CHUNK_SIZE;

                    if (progressCallback != null) {
                        progressCallback.progressUpdate(uploaded, size);
                    }
                }

                UploadSessionCursor cursor = new UploadSessionCursor(sessionId, uploaded);

                // (2) Append
                while ((size - uploaded) > CHUNKED_UPLOAD_CHUNK_SIZE) {
                    dbxClient.files().uploadSessionAppendV2(cursor).uploadAndFinish(in, CHUNKED_UPLOAD_CHUNK_SIZE);
                    uploaded += CHUNKED_UPLOAD_CHUNK_SIZE;
                    if (progressCallback != null) {
                        progressCallback.progressUpdate(uploaded, size);
                    }
                    cursor = new UploadSessionCursor(sessionId, uploaded);
                }

                // (3) Finish
                long remaining = size - uploaded;
                CommitInfo commitInfo = CommitInfo.newBuilder(dropboxPath).withMode(WriteMode.OVERWRITE).withClientModified(new Date(localFile.lastModified())).build();
                FileMetadata metadata = dbxClient.files().uploadSessionFinish(cursor, commitInfo).uploadAndFinish(in, remaining);
                // System.out.println(metadata.toStringMultiline());
                return;
            } catch (RetryException ex) {
                thrown = ex;
                // RetryExceptions are never automatically retried by the client for uploads. Must
                // catch this exception even if DbxRequestConfig.getMaxRetries() > 0.
                sleepQuietly(ex.getBackoffMillis());
                continue;
            } catch (NetworkIOException ex) {
                thrown = ex;
                // network issue with Dropbox (maybe a timeout?) try again
                continue;

            } catch (UploadSessionFinishErrorException ex) {
                if (ex.errorValue.isLookupFailed() && ex.errorValue.getLookupFailedValue().isIncorrectOffset()) {
                    thrown = ex;
                    // server offset into the stream doesn't match our offset (uploaded). Seek to
                    // the expected offset according to the server and try again.
                    uploaded = ex.errorValue.getLookupFailedValue().getIncorrectOffsetValue().getCorrectOffset();
                    continue;
                } else {
                    // some other error occurred, give up.
                    AppLog.e("Error uploading to Dropbox: " + ex.getMessage());
                    throw ex;
                }
            } catch (DbxException ex) {
                throw ex;
            } catch (IOException ex) {
                AppLog.e("Error reading from file \"" + localFile + "\": " + ex.getMessage());
                throw ex;
            } //  finally blocks are guaranteed to be executed
            // close() can throw an IOException too, so we got to wrap that too
            // handle an exception, or often we just ignore it
        }

    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            // just die
            System.err.println("Error uploading to Dropbox: interrupted during backoff.");
        }
    }


    public static class GetCurrentUserAsyncTask extends AsyncTask<Void, Void, DbxUserInfo> {

        private WeakReference<Context> mCtx;
        private Exception mException = null;

        public GetCurrentUserAsyncTask(Context context) {
            this.mCtx = new WeakReference<>(context);
        }

        @Override
        protected DbxUserInfo doInBackground(Void... params) {

            Context context = mCtx.get();
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

            DbxUserInfo dbxUserInfo = null;
            SpaceUsage usage = null;
            FullAccount fullacount = null;

            if( DropboxAccountManager.getDropboxClient(context) == null)
                return null;

            try {
                usage = DropboxAccountManager.getDropboxClient(context).users().getSpaceUsage();
                fullacount = DropboxAccountManager.getDropboxClient(context).users().getCurrentAccount();

                if (sharedPrefs.getString(DropboxAccountManager.PREF_DROPBOX_EMAIL, null) == null) {
                    sharedPrefs.edit().putString(DropboxAccountManager.PREF_DROPBOX_EMAIL, fullacount.getEmail()).apply();
                    AppLog.e("executing sendDbxDat⁄aAsync" + fullacount.getEmail());

                    String signedInEmail = MyApp.getInstance().userMgr.getSignedInEmail();
                    String dbxUIDv1 = sharedPrefs.getString(DropboxAccountManager.PREF_DROPBOX_UID_V1, null);
                    String dbxToken = sharedPrefs.getString(DropboxAccountManager.PREF_DROPBOX_TOKEN, null);

                    SendDbxDataAsync sendDbxDataAsync = new SendDbxDataAsync(signedInEmail, fullacount.getEmail(), dbxUIDv1, dbxToken);
                    // Execute on a separate thread
                    Static.startMyTask(sendDbxDataAsync);
                }

                dbxUserInfo = new DbxUserInfo(usage, fullacount);
                return dbxUserInfo;
            } catch (InvalidAccessTokenException e) {
                AppLog.e(String.format("Invalid Token: %s", e.getMessage()));
                // Something is wrong, we need to reset the token
                // set the current token as last revoked
                sharedPrefs.edit().putString(PREF_DROPBOX_LAST_REVOKED_TOKEN, getToken(context)).apply();
                DropboxLocalHelper.clearLocalDropboxData(context);

                sDbxClient = null;
                sDbxLongpollClient = null;

                return null;
            } catch (DbxException e) {
                AppLog.e(String.format("Error getting current Dropbox account: %s", e.getMessage()));
                return null;
            }

        }
    }


}

