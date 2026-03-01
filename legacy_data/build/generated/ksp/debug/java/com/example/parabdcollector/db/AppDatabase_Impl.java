package com.example.parabdcollector.data;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.example.parabdcollector.dao.CollectionDao;
import com.example.parabdcollector.dao.CollectionDao_Impl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile CollectionDao _collectionDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(11) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `collection_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `remoteId` INTEGER, `titre` TEXT NOT NULL, `editeur` TEXT, `annee` INTEGER, `mois` INTEGER, `categorie` TEXT, `superCategorie` TEXT, `materiau` TEXT, `tirage` TEXT, `dimensions` TEXT, `prixAchat` REAL, `valeurEstimee` REAL, `lieuAchat` TEXT, `description` TEXT, `imageUri` TEXT, `localisation` TEXT, `imageEmbedding` BLOB, `isPossessed` INTEGER NOT NULL)");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_collection_items_remoteId` ON `collection_items` (`remoteId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '4aedeafb3851614cf3ff7b7136686d88')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `collection_items`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsCollectionItems = new HashMap<String, TableInfo.Column>(19);
        _columnsCollectionItems.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCollectionItems.put("remoteId", new TableInfo.Column("remoteId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCollectionItems.put("titre", new TableInfo.Column("titre", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCollectionItems.put("editeur", new TableInfo.Column("editeur", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCollectionItems.put("annee", new TableInfo.Column("annee", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCollectionItems.put("mois", new TableInfo.Column("mois", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCollectionItems.put("categorie", new TableInfo.Column("categorie", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCollectionItems.put("superCategorie", new TableInfo.Column("superCategorie", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCollectionItems.put("materiau", new TableInfo.Column("materiau", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCollectionItems.put("tirage", new TableInfo.Column("tirage", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCollectionItems.put("dimensions", new TableInfo.Column("dimensions", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCollectionItems.put("prixAchat", new TableInfo.Column("prixAchat", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCollectionItems.put("valeurEstimee", new TableInfo.Column("valeurEstimee", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCollectionItems.put("lieuAchat", new TableInfo.Column("lieuAchat", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCollectionItems.put("description", new TableInfo.Column("description", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCollectionItems.put("imageUri", new TableInfo.Column("imageUri", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCollectionItems.put("localisation", new TableInfo.Column("localisation", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCollectionItems.put("imageEmbedding", new TableInfo.Column("imageEmbedding", "BLOB", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCollectionItems.put("isPossessed", new TableInfo.Column("isPossessed", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCollectionItems = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCollectionItems = new HashSet<TableInfo.Index>(1);
        _indicesCollectionItems.add(new TableInfo.Index("index_collection_items_remoteId", true, Arrays.asList("remoteId"), Arrays.asList("ASC")));
        final TableInfo _infoCollectionItems = new TableInfo("collection_items", _columnsCollectionItems, _foreignKeysCollectionItems, _indicesCollectionItems);
        final TableInfo _existingCollectionItems = TableInfo.read(db, "collection_items");
        if (!_infoCollectionItems.equals(_existingCollectionItems)) {
          return new RoomOpenHelper.ValidationResult(false, "collection_items(com.example.legacy_data.model.CollectionItem).\n"
                  + " Expected:\n" + _infoCollectionItems + "\n"
                  + " Found:\n" + _existingCollectionItems);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "4aedeafb3851614cf3ff7b7136686d88", "c30dce5f6f0024652fd5b4551c34a2db");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "collection_items");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `collection_items`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(CollectionDao.class, CollectionDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public CollectionDao collectionDao() {
    if (_collectionDao != null) {
      return _collectionDao;
    } else {
      synchronized(this) {
        if(_collectionDao == null) {
          _collectionDao = new CollectionDao_Impl(this);
        }
        return _collectionDao;
      }
    }
  }
}
