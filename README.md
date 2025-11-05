# DiaroAndroid


```java
  // Get the filtered entries
  // filter is-> AND e.date>=1579820400000 AND e.date<=1579906799999 AND e.folder_uid='ac89bebdb2db752530be26942606283b'
  // AND (e.tags LIKE '%,4504ad8cfc52f31ac832062334baaf5f,%' OR e.tags LIKE '%,ef1372470a7d3f4af8ab115e391cb539,%')
  // AND (e.location_uid='4ca7097310ffcce2696830660ee9f8a3') AND e.title || e.text LIKE ?
  // second part is-> [%mysearch%]
    Pair<String, String[]> pair = EntriesStatic.getEntriesAndSqlByActiveFilters(null);
    Cursor cursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getEntriesCursorUidsOnly(pair.first, pair.second);
    int entryUidColumnIndex = cursor.getColumnIndex(Tables.KEY_UID);
    while (cursor.moveToNext()) {
      entriesUidsArrayList.add(cursor.getString(entryUidColumnIndex));
    }
    cursor.close();
  ```
