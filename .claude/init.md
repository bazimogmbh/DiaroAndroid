# Diaro Android - Claude Code Context

Welcome to the Diaro Android codebase! This file provides essential context for working with this project.

---

## Project Overview

**Diaro** is a feature-rich, encrypted diary/journal Android application with:
- End-to-end encryption (SQLCipher + AES-256)
- Cloud sync via Dropbox
- Rich media support (photos, locations, weather, moods)
- Import/export from 10+ diary apps
- Premium features via in-app purchases

**Current Version:** 3.93.3 (Build 423)
**Min SDK:** 23 | **Target SDK:** 34
**Languages:** Java (primary) + Kotlin
**Build System:** Gradle 8.x with AGP 8.13.0

---

## Quick Navigation

### 📁 Key Directories

```
app/src/main/java/com/pixelcrater/Diaro/
├── entries/          - Core diary entry functionality
│   ├── viewedit/    - Entry UI (EntryViewEditActivity, EntryFragment)
│   └── async/       - Background operations
├── storage/          - Data persistence
│   ├── sqlite/      - Local encrypted database (SQLCipher)
│   └── dropbox/     - Cloud sync implementation
├── premium/          - In-app billing (Google Play Billing 8.0.0)
├── profile/          - User authentication & Dropbox OAuth
├── utils/            - Utilities (Static.java, AES256Cipher, etc.)
└── moods/            - Mood tracking feature
```

### 📊 Database Schema

**7 Main Tables:**
1. `diaro_entries` - Diary entries
2. `diaro_attachments` - Photos/media
3. `diaro_folders` - Categories
4. `diaro_tags` - Entry tags
5. `diaro_locations` - Saved places
6. `diaro_moods` - Mood tracking
7. `diaro_templates` - Entry templates

**Encryption:** SQLCipher 4.10.0 + AES-256 for sensitive data

### 🔄 Sync Architecture

```
Dropbox SDK 7.0.0
    ↓
OAuth 2.0 Authentication (ProfileActivity.java)
    ↓
SyncService / SyncWorker
    ↓
├── UploadJsonFiles.java (batch upload entries)
├── UploadAttachments.java (batch upload media)
├── DownloadJsonFiles.java (download entry metadata)
└── DownloadPhotos.java (download media)
```

**Key Classes:**
- `DropboxAccountManager.java` - OAuth & token management
- `SyncAsync.java` - Main sync orchestrator
- `DropboxLocalHelper.java` - Helper methods

---

## 🐛 Known Issues & Technical Debt

### Critical Bugs (Must Fix)
See `BUGS_AND_PERFORMANCE.md` for complete list. Top 5:

1. **Resource Leaks:** Cursors not closed in exception paths
   - `UploadJsonFiles.java:159-180`
   - `DownloadJsonJob.java:65`

2. **Thread Safety:** Unsynchronized access to shared collections
   - `UploadJsonFiles.java:237` - mUploadSessionFinishArgList
   - `UploadAttachments.java:56` - uploadFilesSet HashMap

3. **Memory Leaks:** SharedPreferences listeners not unregistered
   - `AppMainActivity.java:285`

4. **NPE Risks:** Missing null checks
   - `UploadAttachments.java:106` - rowID could be null
   - `DownloadJsonJob.java:71` - cv could be null

5. **Infinite Recursion:** checkDropboxToken() recursive call
   - `DropboxLocalHelper.java:196`

### Performance Issues

1. **Missing Transactions:** Batch DB updates without transactions (10-50x slower)
   - `EntriesStatic.java:29-140`

2. **String Concatenation in Loop:** O(n²) complexity
   - `EntriesStatic.java:113-123`

3. **Inefficient Queries:** LIKE queries prevent index usage
   - `SQLiteAdapter.java:645-675`

4. **AsyncTask Deprecated:** 60+ files need migration to Coroutines

### Technical Debt

- Mixed Java/Kotlin codebase
- ProGuard/R8 disabled in release builds
- Large utility class (`Static.java` - 63K+ lines)
- Limited test coverage

---

## 🔑 Important Patterns & Conventions

### Singleton Access Pattern
```java
// Always access managers through MyApp singleton
MyApp.getInstance().storageMgr.getSQLiteAdapter()
MyApp.getInstance().userMgr.isSignedIn()
MyApp.getInstance().securityCodeMgr.isSecurityCodeSet()
```

### Database Operations
```java
// Always close cursors!
Cursor cursor = null;
try {
    cursor = adapter.getRowsCursor(...);
    // ... use cursor
} finally {
    if (cursor != null) cursor.close();
}

// Use transactions for batch operations
db.beginTransaction();
try {
    // ... multiple updates
    db.setTransactionSuccessful();
} finally {
    db.endTransaction();
}
```

### Dropbox Batch Upload Pattern
```java
// 1. Start sessions and upload data (keep open)
UploadSessionStartResult result = dbxClient.files()
    .uploadSessionStart()
    .upload(inputStream, fileSize); // Use upload(), NOT uploadAndFinish()

// 2. Collect session args
mUploadSessionFinishArgList.add(new UploadSessionFinishArg(
    new UploadSessionCursor(result.getSessionId(), fileSize),
    commitInfo
));

// 3. Batch commit all at once
UploadSessionFinishBatchResult result =
    dbxClient.files().uploadSessionFinishBatchV2(mUploadSessionFinishArgList);
```

### Error Handling
```java
// Don't swallow exceptions!
try {
    // ... risky operation
} catch (Exception e) {
    AppLog.e("Descriptive error message", e); // Always log
    // Handle or rethrow
}
```

---

## 🔐 Security Best Practices

### Encryption Keys
```java
// Never log encryption keys or encrypted data
// ❌ BAD
AppLog.e("uploading-> " + encryptedData);

// ✅ GOOD
if (BuildConfig.DEBUG) {
    AppLog.d("Uploading entry: " + entryId);
}
```

### OAuth Tokens
- Tokens stored in SharedPreferences
- UID (PREF_DROPBOX_UID_V1) used as encryption key
- Must be captured in ProfileActivity.onResume()

### Database Encryption
- SQLCipher encrypts entire database
- Password derived from device UID
- Additional AES-256 for sensitive fields

---

## 📝 Common Tasks

### Adding a New Entry Field

1. **Update Database Schema**
   ```java
   // In SQLiteOpenHelperCipher
   db.execSQL("ALTER TABLE diaro_entries ADD COLUMN new_field TEXT");
   ```

2. **Update Model**
   ```java
   // In EntryInfo.java
   public String newField;
   ```

3. **Update Adapter**
   ```java
   // In SQLiteAdapter.java
   cv.put("new_field", value);
   ```

4. **Update Sync**
   ```java
   // In DropboxStatic.java - createJsonString()
   jsonObject.put("new_field", cursor.getString(...));
   ```

### Debugging Dropbox Sync

```bash
# Filter logcat for sync operations
adb logcat | grep -E "(Diaro|Dropbox|Sync)"

# Watch for specific errors
adb logcat | grep -E "Upload|Download|not_closed|lookup_failed"
```

**Common Sync Errors:**
- `not_closed` - Using uploadAndFinish() instead of upload() for batch
- `lookup_failed` - Session ID not found or already committed
- `path/not_found` - File doesn't exist on Dropbox

### Testing Premium Features

```java
// Temporarily bypass premium check
if (BuildConfig.DEBUG || Static.isProUser()) {
    // Premium feature code
}

// Or manually set in prefs
MyApp.getInstance().prefs.edit()
    .putBoolean(Prefs.PREF_PRO, true)
    .apply();
```

---

## 🛠️ Build & Run

### Build Variants

```bash
# Debug build
./gradlew assembleDebug

# Release build (ProGuard disabled)
./gradlew assembleRelease

# Install debug to device
./gradlew installDebug

# Run all lint checks
./gradlew lint
```

### Module Structure

```
DiaroAndroid/
├── app/              - Main application module
├── feat-moods/       - Mood tracking feature
├── owmweather/       - Weather integration
├── piceditor/        - Photo editing
├── demodata/         - Sample data
└── yanzhenjie-album/ - Photo picker
```

### Key Dependencies

```gradle
// Core
implementation 'net.zetetic:sqlcipher-android:4.10.0'
implementation 'com.dropbox.core:dropbox-core-sdk:7.0.0'
implementation 'com.github.bumptech.glide:glide:5.0.5'

// Billing
implementation 'com.android.billingclient:billing:8.0.0'

// Firebase
implementation platform('com.google.firebase:firebase-bom:34.5.0')
```

---

## 🧪 Testing

### Current State
- Manual testing primarily
- Firebase Crashlytics for error tracking
- Limited unit test coverage

### Recommended Approach
```kotlin
// Unit test example
@Test
fun `test entry creation with encryption`() {
    val entry = EntryInfo().apply {
        title = "Test Entry"
        text = "Secret content"
    }

    val encrypted = AES256Cipher.encodeString(entry.text, testKey)
    assertNotEquals(entry.text, encrypted)

    val decrypted = AES256Cipher.decodeString(encrypted, testKey)
    assertEquals(entry.text, decrypted)
}
```

---

## 📚 Resources

### Documentation
- **Architecture:** `ARCHITECTURE.md` - Complete system design
- **Bugs & Performance:** `BUGS_AND_PERFORMANCE.md` - Known issues
- **API Docs:** Inline Javadoc throughout codebase

### External Links
- [Dropbox SDK Docs](https://dropbox.github.io/dropbox-sdk-java/)
- [SQLCipher for Android](https://www.zetetic.net/sqlcipher/sqlcipher-for-android/)
- [Google Play Billing](https://developer.android.com/google/play/billing)

### Useful Commands

```bash
# Find all TODO comments
grep -r "TODO" app/src/main/java/

# Find AsyncTask usage (deprecated)
grep -r "extends AsyncTask" app/src/main/java/

# Find cursor operations
grep -r "\.close()" app/src/main/java/ | grep Cursor

# Find potential NPE
grep -r "\.get(" app/src/main/java/ | grep -v "null"
```

---

## 🤝 Contributing Guidelines

### Code Style
- Follow existing Java conventions
- Use 4-space indentation
- Always close resources (cursors, streams, etc.)
- Add meaningful comments for complex logic
- Use descriptive variable names

### Before Committing
1. ✅ Run lint: `./gradlew lint`
2. ✅ Check for resource leaks
3. ✅ Verify cursor.close() in finally blocks
4. ✅ Test sync functionality
5. ✅ Check for sensitive data in logs

### Pull Request Checklist
- [ ] No new AsyncTask usage (use Coroutines)
- [ ] Database operations use transactions
- [ ] Resources properly closed
- [ ] Null checks for external data
- [ ] Thread-safe access to shared data
- [ ] No sensitive data in logs
- [ ] Updated BUGS_AND_PERFORMANCE.md if fixing issues

---

## 🆘 Getting Help

### Common Problems

**Q: Build fails with "Duplicate class" error**
```bash
# Clean and rebuild
./gradlew clean
./gradlew assembleDebug
```

**Q: Sync fails with "not_closed" error**
A: Check UploadJsonFiles.java and UploadAttachments.java - use `.upload()` not `.uploadAndFinish()` for batch operations

**Q: Database locked error**
A: Ensure all cursors are closed, use transactions properly

**Q: Memory leaks in AsyncTask**
A: Migrate to Kotlin Coroutines with ViewModelScope

### Debug Flags

```java
// Enable verbose logging
AppConfig.DEBUG_MODE = true;

// Skip authentication (debug only)
AppConfig.SKIP_AUTH = true;

// Use plain JSON (no encryption) for easier debugging
AppConfig.USE_PLAIN_JSON = true;
```

---

## 📊 Performance Tips

### Database
- Use transactions for batch operations
- Add indexes for frequently queried columns
- Close cursors immediately after use
- Cache frequently accessed data

### Sync
- Batch operations when possible
- Use delta sync instead of full sync
- Implement exponential backoff for retries
- Stream large files instead of loading to memory

### UI
- Use RecyclerView with DiffUtil
- Load images with Glide (already configured)
- Avoid blocking main thread
- Use ViewHolder pattern correctly

---

## 🔄 Recent Changes

### Latest Migration Work (2025-11-05)
- ✅ Migrated Google Play Billing to 8.0.0 (ProductDetails API)
- ✅ Fixed switch-case statements (R.id non-final issue)
- ✅ Updated Dropbox batch upload to use uploadSessionFinishBatchV2
- ✅ Added null checks for UploadJsonFiles and DownloadJsonJob
- ✅ Fixed Dropbox OAuth UID capture in ProfileActivity.onResume()

### Known Regressions
- None currently

---

## 🎯 Priority Tasks

### Immediate (This Week)
1. Fix resource leaks (cursors, streams)
2. Add ConcurrentHashMap for thread safety
3. Add transactions to batch DB operations

### Short Term (This Month)
4. Migrate AsyncTask to Coroutines
5. Enable ProGuard/R8
6. Add database indexes

### Long Term (This Quarter)
7. Full Kotlin migration
8. Comprehensive test suite
9. Architecture Components (ViewModel, LiveData)

---

*This file is maintained by the development team. Last updated: 2025-11-05*
*For questions or clarifications, refer to ARCHITECTURE.md or BUGS_AND_PERFORMANCE.md*
