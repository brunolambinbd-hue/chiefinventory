package com.example.parabdcollector.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.example.parabdcollector.model.CategoryInfo;
import com.example.parabdcollector.model.CollectionItem;
import java.lang.Class;
import java.lang.Double;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class CollectionDao_Impl implements CollectionDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<CollectionItem> __insertionAdapterOfCollectionItem;

  private final EntityDeletionOrUpdateAdapter<CollectionItem> __deletionAdapterOfCollectionItem;

  private final EntityDeletionOrUpdateAdapter<CollectionItem> __updateAdapterOfCollectionItem;

  public CollectionDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfCollectionItem = new EntityInsertionAdapter<CollectionItem>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR IGNORE INTO `collection_items` (`id`,`remoteId`,`titre`,`editeur`,`annee`,`mois`,`categorie`,`superCategorie`,`materiau`,`tirage`,`dimensions`,`prixAchat`,`valeurEstimee`,`lieuAchat`,`description`,`imageUri`,`localisation`,`imageEmbedding`,`isPossessed`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CollectionItem entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getRemoteId() == null) {
          statement.bindNull(2);
        } else {
          statement.bindLong(2, entity.getRemoteId());
        }
        statement.bindString(3, entity.getTitre());
        if (entity.getEditeur() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getEditeur());
        }
        if (entity.getAnnee() == null) {
          statement.bindNull(5);
        } else {
          statement.bindLong(5, entity.getAnnee());
        }
        if (entity.getMois() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getMois());
        }
        if (entity.getCategorie() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getCategorie());
        }
        if (entity.getSuperCategorie() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getSuperCategorie());
        }
        if (entity.getMateriau() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getMateriau());
        }
        if (entity.getTirage() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getTirage());
        }
        if (entity.getDimensions() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getDimensions());
        }
        if (entity.getPrixAchat() == null) {
          statement.bindNull(12);
        } else {
          statement.bindDouble(12, entity.getPrixAchat());
        }
        if (entity.getValeurEstimee() == null) {
          statement.bindNull(13);
        } else {
          statement.bindDouble(13, entity.getValeurEstimee());
        }
        if (entity.getLieuAchat() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getLieuAchat());
        }
        if (entity.getDescription() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getDescription());
        }
        if (entity.getImageUri() == null) {
          statement.bindNull(16);
        } else {
          statement.bindString(16, entity.getImageUri());
        }
        if (entity.getLocalisation() == null) {
          statement.bindNull(17);
        } else {
          statement.bindString(17, entity.getLocalisation());
        }
        if (entity.getImageEmbedding() == null) {
          statement.bindNull(18);
        } else {
          statement.bindBlob(18, entity.getImageEmbedding());
        }
        final int _tmp = entity.isPossessed() ? 1 : 0;
        statement.bindLong(19, _tmp);
      }
    };
    this.__deletionAdapterOfCollectionItem = new EntityDeletionOrUpdateAdapter<CollectionItem>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `collection_items` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CollectionItem entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfCollectionItem = new EntityDeletionOrUpdateAdapter<CollectionItem>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `collection_items` SET `id` = ?,`remoteId` = ?,`titre` = ?,`editeur` = ?,`annee` = ?,`mois` = ?,`categorie` = ?,`superCategorie` = ?,`materiau` = ?,`tirage` = ?,`dimensions` = ?,`prixAchat` = ?,`valeurEstimee` = ?,`lieuAchat` = ?,`description` = ?,`imageUri` = ?,`localisation` = ?,`imageEmbedding` = ?,`isPossessed` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CollectionItem entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getRemoteId() == null) {
          statement.bindNull(2);
        } else {
          statement.bindLong(2, entity.getRemoteId());
        }
        statement.bindString(3, entity.getTitre());
        if (entity.getEditeur() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getEditeur());
        }
        if (entity.getAnnee() == null) {
          statement.bindNull(5);
        } else {
          statement.bindLong(5, entity.getAnnee());
        }
        if (entity.getMois() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getMois());
        }
        if (entity.getCategorie() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getCategorie());
        }
        if (entity.getSuperCategorie() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getSuperCategorie());
        }
        if (entity.getMateriau() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getMateriau());
        }
        if (entity.getTirage() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getTirage());
        }
        if (entity.getDimensions() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getDimensions());
        }
        if (entity.getPrixAchat() == null) {
          statement.bindNull(12);
        } else {
          statement.bindDouble(12, entity.getPrixAchat());
        }
        if (entity.getValeurEstimee() == null) {
          statement.bindNull(13);
        } else {
          statement.bindDouble(13, entity.getValeurEstimee());
        }
        if (entity.getLieuAchat() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getLieuAchat());
        }
        if (entity.getDescription() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getDescription());
        }
        if (entity.getImageUri() == null) {
          statement.bindNull(16);
        } else {
          statement.bindString(16, entity.getImageUri());
        }
        if (entity.getLocalisation() == null) {
          statement.bindNull(17);
        } else {
          statement.bindString(17, entity.getLocalisation());
        }
        if (entity.getImageEmbedding() == null) {
          statement.bindNull(18);
        } else {
          statement.bindBlob(18, entity.getImageEmbedding());
        }
        final int _tmp = entity.isPossessed() ? 1 : 0;
        statement.bindLong(19, _tmp);
        statement.bindLong(20, entity.getId());
      }
    };
  }

  @Override
  public Object insert(final CollectionItem item, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfCollectionItem.insert(item);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final CollectionItem item, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfCollectionItem.handle(item);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final CollectionItem item, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfCollectionItem.handle(item);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public LiveData<List<CollectionItem>> getAllPossessed() {
    final String _sql = "SELECT * FROM collection_items WHERE isPossessed = 1 ORDER BY titre ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"collection_items"}, false, new Callable<List<CollectionItem>>() {
      @Override
      @Nullable
      public List<CollectionItem> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfRemoteId = CursorUtil.getColumnIndexOrThrow(_cursor, "remoteId");
          final int _cursorIndexOfTitre = CursorUtil.getColumnIndexOrThrow(_cursor, "titre");
          final int _cursorIndexOfEditeur = CursorUtil.getColumnIndexOrThrow(_cursor, "editeur");
          final int _cursorIndexOfAnnee = CursorUtil.getColumnIndexOrThrow(_cursor, "annee");
          final int _cursorIndexOfMois = CursorUtil.getColumnIndexOrThrow(_cursor, "mois");
          final int _cursorIndexOfCategorie = CursorUtil.getColumnIndexOrThrow(_cursor, "categorie");
          final int _cursorIndexOfSuperCategorie = CursorUtil.getColumnIndexOrThrow(_cursor, "superCategorie");
          final int _cursorIndexOfMateriau = CursorUtil.getColumnIndexOrThrow(_cursor, "materiau");
          final int _cursorIndexOfTirage = CursorUtil.getColumnIndexOrThrow(_cursor, "tirage");
          final int _cursorIndexOfDimensions = CursorUtil.getColumnIndexOrThrow(_cursor, "dimensions");
          final int _cursorIndexOfPrixAchat = CursorUtil.getColumnIndexOrThrow(_cursor, "prixAchat");
          final int _cursorIndexOfValeurEstimee = CursorUtil.getColumnIndexOrThrow(_cursor, "valeurEstimee");
          final int _cursorIndexOfLieuAchat = CursorUtil.getColumnIndexOrThrow(_cursor, "lieuAchat");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfImageUri = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUri");
          final int _cursorIndexOfLocalisation = CursorUtil.getColumnIndexOrThrow(_cursor, "localisation");
          final int _cursorIndexOfImageEmbedding = CursorUtil.getColumnIndexOrThrow(_cursor, "imageEmbedding");
          final int _cursorIndexOfIsPossessed = CursorUtil.getColumnIndexOrThrow(_cursor, "isPossessed");
          final List<CollectionItem> _result = new ArrayList<CollectionItem>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CollectionItem _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final Integer _tmpRemoteId;
            if (_cursor.isNull(_cursorIndexOfRemoteId)) {
              _tmpRemoteId = null;
            } else {
              _tmpRemoteId = _cursor.getInt(_cursorIndexOfRemoteId);
            }
            final String _tmpTitre;
            _tmpTitre = _cursor.getString(_cursorIndexOfTitre);
            final String _tmpEditeur;
            if (_cursor.isNull(_cursorIndexOfEditeur)) {
              _tmpEditeur = null;
            } else {
              _tmpEditeur = _cursor.getString(_cursorIndexOfEditeur);
            }
            final Integer _tmpAnnee;
            if (_cursor.isNull(_cursorIndexOfAnnee)) {
              _tmpAnnee = null;
            } else {
              _tmpAnnee = _cursor.getInt(_cursorIndexOfAnnee);
            }
            final Integer _tmpMois;
            if (_cursor.isNull(_cursorIndexOfMois)) {
              _tmpMois = null;
            } else {
              _tmpMois = _cursor.getInt(_cursorIndexOfMois);
            }
            final String _tmpCategorie;
            if (_cursor.isNull(_cursorIndexOfCategorie)) {
              _tmpCategorie = null;
            } else {
              _tmpCategorie = _cursor.getString(_cursorIndexOfCategorie);
            }
            final String _tmpSuperCategorie;
            if (_cursor.isNull(_cursorIndexOfSuperCategorie)) {
              _tmpSuperCategorie = null;
            } else {
              _tmpSuperCategorie = _cursor.getString(_cursorIndexOfSuperCategorie);
            }
            final String _tmpMateriau;
            if (_cursor.isNull(_cursorIndexOfMateriau)) {
              _tmpMateriau = null;
            } else {
              _tmpMateriau = _cursor.getString(_cursorIndexOfMateriau);
            }
            final String _tmpTirage;
            if (_cursor.isNull(_cursorIndexOfTirage)) {
              _tmpTirage = null;
            } else {
              _tmpTirage = _cursor.getString(_cursorIndexOfTirage);
            }
            final String _tmpDimensions;
            if (_cursor.isNull(_cursorIndexOfDimensions)) {
              _tmpDimensions = null;
            } else {
              _tmpDimensions = _cursor.getString(_cursorIndexOfDimensions);
            }
            final Double _tmpPrixAchat;
            if (_cursor.isNull(_cursorIndexOfPrixAchat)) {
              _tmpPrixAchat = null;
            } else {
              _tmpPrixAchat = _cursor.getDouble(_cursorIndexOfPrixAchat);
            }
            final Double _tmpValeurEstimee;
            if (_cursor.isNull(_cursorIndexOfValeurEstimee)) {
              _tmpValeurEstimee = null;
            } else {
              _tmpValeurEstimee = _cursor.getDouble(_cursorIndexOfValeurEstimee);
            }
            final String _tmpLieuAchat;
            if (_cursor.isNull(_cursorIndexOfLieuAchat)) {
              _tmpLieuAchat = null;
            } else {
              _tmpLieuAchat = _cursor.getString(_cursorIndexOfLieuAchat);
            }
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final String _tmpImageUri;
            if (_cursor.isNull(_cursorIndexOfImageUri)) {
              _tmpImageUri = null;
            } else {
              _tmpImageUri = _cursor.getString(_cursorIndexOfImageUri);
            }
            final String _tmpLocalisation;
            if (_cursor.isNull(_cursorIndexOfLocalisation)) {
              _tmpLocalisation = null;
            } else {
              _tmpLocalisation = _cursor.getString(_cursorIndexOfLocalisation);
            }
            final byte[] _tmpImageEmbedding;
            if (_cursor.isNull(_cursorIndexOfImageEmbedding)) {
              _tmpImageEmbedding = null;
            } else {
              _tmpImageEmbedding = _cursor.getBlob(_cursorIndexOfImageEmbedding);
            }
            final boolean _tmpIsPossessed;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsPossessed);
            _tmpIsPossessed = _tmp != 0;
            _item = new CollectionItem(_tmpId,_tmpRemoteId,_tmpTitre,_tmpEditeur,_tmpAnnee,_tmpMois,_tmpCategorie,_tmpSuperCategorie,_tmpMateriau,_tmpTirage,_tmpDimensions,_tmpPrixAchat,_tmpValeurEstimee,_tmpLieuAchat,_tmpDescription,_tmpImageUri,_tmpLocalisation,_tmpImageEmbedding,_tmpIsPossessed);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public LiveData<List<CollectionItem>> getAllSought() {
    final String _sql = "SELECT * FROM collection_items WHERE isPossessed = 0 ORDER BY titre ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"collection_items"}, false, new Callable<List<CollectionItem>>() {
      @Override
      @Nullable
      public List<CollectionItem> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfRemoteId = CursorUtil.getColumnIndexOrThrow(_cursor, "remoteId");
          final int _cursorIndexOfTitre = CursorUtil.getColumnIndexOrThrow(_cursor, "titre");
          final int _cursorIndexOfEditeur = CursorUtil.getColumnIndexOrThrow(_cursor, "editeur");
          final int _cursorIndexOfAnnee = CursorUtil.getColumnIndexOrThrow(_cursor, "annee");
          final int _cursorIndexOfMois = CursorUtil.getColumnIndexOrThrow(_cursor, "mois");
          final int _cursorIndexOfCategorie = CursorUtil.getColumnIndexOrThrow(_cursor, "categorie");
          final int _cursorIndexOfSuperCategorie = CursorUtil.getColumnIndexOrThrow(_cursor, "superCategorie");
          final int _cursorIndexOfMateriau = CursorUtil.getColumnIndexOrThrow(_cursor, "materiau");
          final int _cursorIndexOfTirage = CursorUtil.getColumnIndexOrThrow(_cursor, "tirage");
          final int _cursorIndexOfDimensions = CursorUtil.getColumnIndexOrThrow(_cursor, "dimensions");
          final int _cursorIndexOfPrixAchat = CursorUtil.getColumnIndexOrThrow(_cursor, "prixAchat");
          final int _cursorIndexOfValeurEstimee = CursorUtil.getColumnIndexOrThrow(_cursor, "valeurEstimee");
          final int _cursorIndexOfLieuAchat = CursorUtil.getColumnIndexOrThrow(_cursor, "lieuAchat");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfImageUri = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUri");
          final int _cursorIndexOfLocalisation = CursorUtil.getColumnIndexOrThrow(_cursor, "localisation");
          final int _cursorIndexOfImageEmbedding = CursorUtil.getColumnIndexOrThrow(_cursor, "imageEmbedding");
          final int _cursorIndexOfIsPossessed = CursorUtil.getColumnIndexOrThrow(_cursor, "isPossessed");
          final List<CollectionItem> _result = new ArrayList<CollectionItem>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CollectionItem _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final Integer _tmpRemoteId;
            if (_cursor.isNull(_cursorIndexOfRemoteId)) {
              _tmpRemoteId = null;
            } else {
              _tmpRemoteId = _cursor.getInt(_cursorIndexOfRemoteId);
            }
            final String _tmpTitre;
            _tmpTitre = _cursor.getString(_cursorIndexOfTitre);
            final String _tmpEditeur;
            if (_cursor.isNull(_cursorIndexOfEditeur)) {
              _tmpEditeur = null;
            } else {
              _tmpEditeur = _cursor.getString(_cursorIndexOfEditeur);
            }
            final Integer _tmpAnnee;
            if (_cursor.isNull(_cursorIndexOfAnnee)) {
              _tmpAnnee = null;
            } else {
              _tmpAnnee = _cursor.getInt(_cursorIndexOfAnnee);
            }
            final Integer _tmpMois;
            if (_cursor.isNull(_cursorIndexOfMois)) {
              _tmpMois = null;
            } else {
              _tmpMois = _cursor.getInt(_cursorIndexOfMois);
            }
            final String _tmpCategorie;
            if (_cursor.isNull(_cursorIndexOfCategorie)) {
              _tmpCategorie = null;
            } else {
              _tmpCategorie = _cursor.getString(_cursorIndexOfCategorie);
            }
            final String _tmpSuperCategorie;
            if (_cursor.isNull(_cursorIndexOfSuperCategorie)) {
              _tmpSuperCategorie = null;
            } else {
              _tmpSuperCategorie = _cursor.getString(_cursorIndexOfSuperCategorie);
            }
            final String _tmpMateriau;
            if (_cursor.isNull(_cursorIndexOfMateriau)) {
              _tmpMateriau = null;
            } else {
              _tmpMateriau = _cursor.getString(_cursorIndexOfMateriau);
            }
            final String _tmpTirage;
            if (_cursor.isNull(_cursorIndexOfTirage)) {
              _tmpTirage = null;
            } else {
              _tmpTirage = _cursor.getString(_cursorIndexOfTirage);
            }
            final String _tmpDimensions;
            if (_cursor.isNull(_cursorIndexOfDimensions)) {
              _tmpDimensions = null;
            } else {
              _tmpDimensions = _cursor.getString(_cursorIndexOfDimensions);
            }
            final Double _tmpPrixAchat;
            if (_cursor.isNull(_cursorIndexOfPrixAchat)) {
              _tmpPrixAchat = null;
            } else {
              _tmpPrixAchat = _cursor.getDouble(_cursorIndexOfPrixAchat);
            }
            final Double _tmpValeurEstimee;
            if (_cursor.isNull(_cursorIndexOfValeurEstimee)) {
              _tmpValeurEstimee = null;
            } else {
              _tmpValeurEstimee = _cursor.getDouble(_cursorIndexOfValeurEstimee);
            }
            final String _tmpLieuAchat;
            if (_cursor.isNull(_cursorIndexOfLieuAchat)) {
              _tmpLieuAchat = null;
            } else {
              _tmpLieuAchat = _cursor.getString(_cursorIndexOfLieuAchat);
            }
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final String _tmpImageUri;
            if (_cursor.isNull(_cursorIndexOfImageUri)) {
              _tmpImageUri = null;
            } else {
              _tmpImageUri = _cursor.getString(_cursorIndexOfImageUri);
            }
            final String _tmpLocalisation;
            if (_cursor.isNull(_cursorIndexOfLocalisation)) {
              _tmpLocalisation = null;
            } else {
              _tmpLocalisation = _cursor.getString(_cursorIndexOfLocalisation);
            }
            final byte[] _tmpImageEmbedding;
            if (_cursor.isNull(_cursorIndexOfImageEmbedding)) {
              _tmpImageEmbedding = null;
            } else {
              _tmpImageEmbedding = _cursor.getBlob(_cursorIndexOfImageEmbedding);
            }
            final boolean _tmpIsPossessed;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsPossessed);
            _tmpIsPossessed = _tmp != 0;
            _item = new CollectionItem(_tmpId,_tmpRemoteId,_tmpTitre,_tmpEditeur,_tmpAnnee,_tmpMois,_tmpCategorie,_tmpSuperCategorie,_tmpMateriau,_tmpTirage,_tmpDimensions,_tmpPrixAchat,_tmpValeurEstimee,_tmpLieuAchat,_tmpDescription,_tmpImageUri,_tmpLocalisation,_tmpImageEmbedding,_tmpIsPossessed);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public LiveData<Integer> getTotalCount() {
    final String _sql = "SELECT COUNT(*) FROM collection_items";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"collection_items"}, false, new Callable<Integer>() {
      @Override
      @Nullable
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final Integer _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(0);
            }
            _result = _tmp;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public LiveData<CollectionItem> getById(final long id) {
    final String _sql = "SELECT * FROM collection_items WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    return __db.getInvalidationTracker().createLiveData(new String[] {"collection_items"}, false, new Callable<CollectionItem>() {
      @Override
      @Nullable
      public CollectionItem call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfRemoteId = CursorUtil.getColumnIndexOrThrow(_cursor, "remoteId");
          final int _cursorIndexOfTitre = CursorUtil.getColumnIndexOrThrow(_cursor, "titre");
          final int _cursorIndexOfEditeur = CursorUtil.getColumnIndexOrThrow(_cursor, "editeur");
          final int _cursorIndexOfAnnee = CursorUtil.getColumnIndexOrThrow(_cursor, "annee");
          final int _cursorIndexOfMois = CursorUtil.getColumnIndexOrThrow(_cursor, "mois");
          final int _cursorIndexOfCategorie = CursorUtil.getColumnIndexOrThrow(_cursor, "categorie");
          final int _cursorIndexOfSuperCategorie = CursorUtil.getColumnIndexOrThrow(_cursor, "superCategorie");
          final int _cursorIndexOfMateriau = CursorUtil.getColumnIndexOrThrow(_cursor, "materiau");
          final int _cursorIndexOfTirage = CursorUtil.getColumnIndexOrThrow(_cursor, "tirage");
          final int _cursorIndexOfDimensions = CursorUtil.getColumnIndexOrThrow(_cursor, "dimensions");
          final int _cursorIndexOfPrixAchat = CursorUtil.getColumnIndexOrThrow(_cursor, "prixAchat");
          final int _cursorIndexOfValeurEstimee = CursorUtil.getColumnIndexOrThrow(_cursor, "valeurEstimee");
          final int _cursorIndexOfLieuAchat = CursorUtil.getColumnIndexOrThrow(_cursor, "lieuAchat");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfImageUri = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUri");
          final int _cursorIndexOfLocalisation = CursorUtil.getColumnIndexOrThrow(_cursor, "localisation");
          final int _cursorIndexOfImageEmbedding = CursorUtil.getColumnIndexOrThrow(_cursor, "imageEmbedding");
          final int _cursorIndexOfIsPossessed = CursorUtil.getColumnIndexOrThrow(_cursor, "isPossessed");
          final CollectionItem _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final Integer _tmpRemoteId;
            if (_cursor.isNull(_cursorIndexOfRemoteId)) {
              _tmpRemoteId = null;
            } else {
              _tmpRemoteId = _cursor.getInt(_cursorIndexOfRemoteId);
            }
            final String _tmpTitre;
            _tmpTitre = _cursor.getString(_cursorIndexOfTitre);
            final String _tmpEditeur;
            if (_cursor.isNull(_cursorIndexOfEditeur)) {
              _tmpEditeur = null;
            } else {
              _tmpEditeur = _cursor.getString(_cursorIndexOfEditeur);
            }
            final Integer _tmpAnnee;
            if (_cursor.isNull(_cursorIndexOfAnnee)) {
              _tmpAnnee = null;
            } else {
              _tmpAnnee = _cursor.getInt(_cursorIndexOfAnnee);
            }
            final Integer _tmpMois;
            if (_cursor.isNull(_cursorIndexOfMois)) {
              _tmpMois = null;
            } else {
              _tmpMois = _cursor.getInt(_cursorIndexOfMois);
            }
            final String _tmpCategorie;
            if (_cursor.isNull(_cursorIndexOfCategorie)) {
              _tmpCategorie = null;
            } else {
              _tmpCategorie = _cursor.getString(_cursorIndexOfCategorie);
            }
            final String _tmpSuperCategorie;
            if (_cursor.isNull(_cursorIndexOfSuperCategorie)) {
              _tmpSuperCategorie = null;
            } else {
              _tmpSuperCategorie = _cursor.getString(_cursorIndexOfSuperCategorie);
            }
            final String _tmpMateriau;
            if (_cursor.isNull(_cursorIndexOfMateriau)) {
              _tmpMateriau = null;
            } else {
              _tmpMateriau = _cursor.getString(_cursorIndexOfMateriau);
            }
            final String _tmpTirage;
            if (_cursor.isNull(_cursorIndexOfTirage)) {
              _tmpTirage = null;
            } else {
              _tmpTirage = _cursor.getString(_cursorIndexOfTirage);
            }
            final String _tmpDimensions;
            if (_cursor.isNull(_cursorIndexOfDimensions)) {
              _tmpDimensions = null;
            } else {
              _tmpDimensions = _cursor.getString(_cursorIndexOfDimensions);
            }
            final Double _tmpPrixAchat;
            if (_cursor.isNull(_cursorIndexOfPrixAchat)) {
              _tmpPrixAchat = null;
            } else {
              _tmpPrixAchat = _cursor.getDouble(_cursorIndexOfPrixAchat);
            }
            final Double _tmpValeurEstimee;
            if (_cursor.isNull(_cursorIndexOfValeurEstimee)) {
              _tmpValeurEstimee = null;
            } else {
              _tmpValeurEstimee = _cursor.getDouble(_cursorIndexOfValeurEstimee);
            }
            final String _tmpLieuAchat;
            if (_cursor.isNull(_cursorIndexOfLieuAchat)) {
              _tmpLieuAchat = null;
            } else {
              _tmpLieuAchat = _cursor.getString(_cursorIndexOfLieuAchat);
            }
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final String _tmpImageUri;
            if (_cursor.isNull(_cursorIndexOfImageUri)) {
              _tmpImageUri = null;
            } else {
              _tmpImageUri = _cursor.getString(_cursorIndexOfImageUri);
            }
            final String _tmpLocalisation;
            if (_cursor.isNull(_cursorIndexOfLocalisation)) {
              _tmpLocalisation = null;
            } else {
              _tmpLocalisation = _cursor.getString(_cursorIndexOfLocalisation);
            }
            final byte[] _tmpImageEmbedding;
            if (_cursor.isNull(_cursorIndexOfImageEmbedding)) {
              _tmpImageEmbedding = null;
            } else {
              _tmpImageEmbedding = _cursor.getBlob(_cursorIndexOfImageEmbedding);
            }
            final boolean _tmpIsPossessed;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsPossessed);
            _tmpIsPossessed = _tmp != 0;
            _result = new CollectionItem(_tmpId,_tmpRemoteId,_tmpTitre,_tmpEditeur,_tmpAnnee,_tmpMois,_tmpCategorie,_tmpSuperCategorie,_tmpMateriau,_tmpTirage,_tmpDimensions,_tmpPrixAchat,_tmpValeurEstimee,_tmpLieuAchat,_tmpDescription,_tmpImageUri,_tmpLocalisation,_tmpImageEmbedding,_tmpIsPossessed);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public CollectionItem findByRemoteId(final int remoteId) {
    final String _sql = "SELECT * FROM collection_items WHERE remoteId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, remoteId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfRemoteId = CursorUtil.getColumnIndexOrThrow(_cursor, "remoteId");
      final int _cursorIndexOfTitre = CursorUtil.getColumnIndexOrThrow(_cursor, "titre");
      final int _cursorIndexOfEditeur = CursorUtil.getColumnIndexOrThrow(_cursor, "editeur");
      final int _cursorIndexOfAnnee = CursorUtil.getColumnIndexOrThrow(_cursor, "annee");
      final int _cursorIndexOfMois = CursorUtil.getColumnIndexOrThrow(_cursor, "mois");
      final int _cursorIndexOfCategorie = CursorUtil.getColumnIndexOrThrow(_cursor, "categorie");
      final int _cursorIndexOfSuperCategorie = CursorUtil.getColumnIndexOrThrow(_cursor, "superCategorie");
      final int _cursorIndexOfMateriau = CursorUtil.getColumnIndexOrThrow(_cursor, "materiau");
      final int _cursorIndexOfTirage = CursorUtil.getColumnIndexOrThrow(_cursor, "tirage");
      final int _cursorIndexOfDimensions = CursorUtil.getColumnIndexOrThrow(_cursor, "dimensions");
      final int _cursorIndexOfPrixAchat = CursorUtil.getColumnIndexOrThrow(_cursor, "prixAchat");
      final int _cursorIndexOfValeurEstimee = CursorUtil.getColumnIndexOrThrow(_cursor, "valeurEstimee");
      final int _cursorIndexOfLieuAchat = CursorUtil.getColumnIndexOrThrow(_cursor, "lieuAchat");
      final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
      final int _cursorIndexOfImageUri = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUri");
      final int _cursorIndexOfLocalisation = CursorUtil.getColumnIndexOrThrow(_cursor, "localisation");
      final int _cursorIndexOfImageEmbedding = CursorUtil.getColumnIndexOrThrow(_cursor, "imageEmbedding");
      final int _cursorIndexOfIsPossessed = CursorUtil.getColumnIndexOrThrow(_cursor, "isPossessed");
      final CollectionItem _result;
      if (_cursor.moveToFirst()) {
        final long _tmpId;
        _tmpId = _cursor.getLong(_cursorIndexOfId);
        final Integer _tmpRemoteId;
        if (_cursor.isNull(_cursorIndexOfRemoteId)) {
          _tmpRemoteId = null;
        } else {
          _tmpRemoteId = _cursor.getInt(_cursorIndexOfRemoteId);
        }
        final String _tmpTitre;
        _tmpTitre = _cursor.getString(_cursorIndexOfTitre);
        final String _tmpEditeur;
        if (_cursor.isNull(_cursorIndexOfEditeur)) {
          _tmpEditeur = null;
        } else {
          _tmpEditeur = _cursor.getString(_cursorIndexOfEditeur);
        }
        final Integer _tmpAnnee;
        if (_cursor.isNull(_cursorIndexOfAnnee)) {
          _tmpAnnee = null;
        } else {
          _tmpAnnee = _cursor.getInt(_cursorIndexOfAnnee);
        }
        final Integer _tmpMois;
        if (_cursor.isNull(_cursorIndexOfMois)) {
          _tmpMois = null;
        } else {
          _tmpMois = _cursor.getInt(_cursorIndexOfMois);
        }
        final String _tmpCategorie;
        if (_cursor.isNull(_cursorIndexOfCategorie)) {
          _tmpCategorie = null;
        } else {
          _tmpCategorie = _cursor.getString(_cursorIndexOfCategorie);
        }
        final String _tmpSuperCategorie;
        if (_cursor.isNull(_cursorIndexOfSuperCategorie)) {
          _tmpSuperCategorie = null;
        } else {
          _tmpSuperCategorie = _cursor.getString(_cursorIndexOfSuperCategorie);
        }
        final String _tmpMateriau;
        if (_cursor.isNull(_cursorIndexOfMateriau)) {
          _tmpMateriau = null;
        } else {
          _tmpMateriau = _cursor.getString(_cursorIndexOfMateriau);
        }
        final String _tmpTirage;
        if (_cursor.isNull(_cursorIndexOfTirage)) {
          _tmpTirage = null;
        } else {
          _tmpTirage = _cursor.getString(_cursorIndexOfTirage);
        }
        final String _tmpDimensions;
        if (_cursor.isNull(_cursorIndexOfDimensions)) {
          _tmpDimensions = null;
        } else {
          _tmpDimensions = _cursor.getString(_cursorIndexOfDimensions);
        }
        final Double _tmpPrixAchat;
        if (_cursor.isNull(_cursorIndexOfPrixAchat)) {
          _tmpPrixAchat = null;
        } else {
          _tmpPrixAchat = _cursor.getDouble(_cursorIndexOfPrixAchat);
        }
        final Double _tmpValeurEstimee;
        if (_cursor.isNull(_cursorIndexOfValeurEstimee)) {
          _tmpValeurEstimee = null;
        } else {
          _tmpValeurEstimee = _cursor.getDouble(_cursorIndexOfValeurEstimee);
        }
        final String _tmpLieuAchat;
        if (_cursor.isNull(_cursorIndexOfLieuAchat)) {
          _tmpLieuAchat = null;
        } else {
          _tmpLieuAchat = _cursor.getString(_cursorIndexOfLieuAchat);
        }
        final String _tmpDescription;
        if (_cursor.isNull(_cursorIndexOfDescription)) {
          _tmpDescription = null;
        } else {
          _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
        }
        final String _tmpImageUri;
        if (_cursor.isNull(_cursorIndexOfImageUri)) {
          _tmpImageUri = null;
        } else {
          _tmpImageUri = _cursor.getString(_cursorIndexOfImageUri);
        }
        final String _tmpLocalisation;
        if (_cursor.isNull(_cursorIndexOfLocalisation)) {
          _tmpLocalisation = null;
        } else {
          _tmpLocalisation = _cursor.getString(_cursorIndexOfLocalisation);
        }
        final byte[] _tmpImageEmbedding;
        if (_cursor.isNull(_cursorIndexOfImageEmbedding)) {
          _tmpImageEmbedding = null;
        } else {
          _tmpImageEmbedding = _cursor.getBlob(_cursorIndexOfImageEmbedding);
        }
        final boolean _tmpIsPossessed;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsPossessed);
        _tmpIsPossessed = _tmp != 0;
        _result = new CollectionItem(_tmpId,_tmpRemoteId,_tmpTitre,_tmpEditeur,_tmpAnnee,_tmpMois,_tmpCategorie,_tmpSuperCategorie,_tmpMateriau,_tmpTirage,_tmpDimensions,_tmpPrixAchat,_tmpValeurEstimee,_tmpLieuAchat,_tmpDescription,_tmpImageUri,_tmpLocalisation,_tmpImageEmbedding,_tmpIsPossessed);
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public LiveData<List<CollectionItem>> search(final String query) {
    final String _sql = "SELECT * FROM collection_items WHERE titre LIKE ? OR editeur LIKE ? OR CAST(annee AS TEXT) LIKE ? OR categorie LIKE ? OR materiau LIKE ? OR tirage LIKE ? OR dimensions LIKE ? ORDER BY annee DESC, mois DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 7);
    int _argIndex = 1;
    _statement.bindString(_argIndex, query);
    _argIndex = 2;
    _statement.bindString(_argIndex, query);
    _argIndex = 3;
    _statement.bindString(_argIndex, query);
    _argIndex = 4;
    _statement.bindString(_argIndex, query);
    _argIndex = 5;
    _statement.bindString(_argIndex, query);
    _argIndex = 6;
    _statement.bindString(_argIndex, query);
    _argIndex = 7;
    _statement.bindString(_argIndex, query);
    return __db.getInvalidationTracker().createLiveData(new String[] {"collection_items"}, false, new Callable<List<CollectionItem>>() {
      @Override
      @Nullable
      public List<CollectionItem> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfRemoteId = CursorUtil.getColumnIndexOrThrow(_cursor, "remoteId");
          final int _cursorIndexOfTitre = CursorUtil.getColumnIndexOrThrow(_cursor, "titre");
          final int _cursorIndexOfEditeur = CursorUtil.getColumnIndexOrThrow(_cursor, "editeur");
          final int _cursorIndexOfAnnee = CursorUtil.getColumnIndexOrThrow(_cursor, "annee");
          final int _cursorIndexOfMois = CursorUtil.getColumnIndexOrThrow(_cursor, "mois");
          final int _cursorIndexOfCategorie = CursorUtil.getColumnIndexOrThrow(_cursor, "categorie");
          final int _cursorIndexOfSuperCategorie = CursorUtil.getColumnIndexOrThrow(_cursor, "superCategorie");
          final int _cursorIndexOfMateriau = CursorUtil.getColumnIndexOrThrow(_cursor, "materiau");
          final int _cursorIndexOfTirage = CursorUtil.getColumnIndexOrThrow(_cursor, "tirage");
          final int _cursorIndexOfDimensions = CursorUtil.getColumnIndexOrThrow(_cursor, "dimensions");
          final int _cursorIndexOfPrixAchat = CursorUtil.getColumnIndexOrThrow(_cursor, "prixAchat");
          final int _cursorIndexOfValeurEstimee = CursorUtil.getColumnIndexOrThrow(_cursor, "valeurEstimee");
          final int _cursorIndexOfLieuAchat = CursorUtil.getColumnIndexOrThrow(_cursor, "lieuAchat");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfImageUri = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUri");
          final int _cursorIndexOfLocalisation = CursorUtil.getColumnIndexOrThrow(_cursor, "localisation");
          final int _cursorIndexOfImageEmbedding = CursorUtil.getColumnIndexOrThrow(_cursor, "imageEmbedding");
          final int _cursorIndexOfIsPossessed = CursorUtil.getColumnIndexOrThrow(_cursor, "isPossessed");
          final List<CollectionItem> _result = new ArrayList<CollectionItem>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CollectionItem _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final Integer _tmpRemoteId;
            if (_cursor.isNull(_cursorIndexOfRemoteId)) {
              _tmpRemoteId = null;
            } else {
              _tmpRemoteId = _cursor.getInt(_cursorIndexOfRemoteId);
            }
            final String _tmpTitre;
            _tmpTitre = _cursor.getString(_cursorIndexOfTitre);
            final String _tmpEditeur;
            if (_cursor.isNull(_cursorIndexOfEditeur)) {
              _tmpEditeur = null;
            } else {
              _tmpEditeur = _cursor.getString(_cursorIndexOfEditeur);
            }
            final Integer _tmpAnnee;
            if (_cursor.isNull(_cursorIndexOfAnnee)) {
              _tmpAnnee = null;
            } else {
              _tmpAnnee = _cursor.getInt(_cursorIndexOfAnnee);
            }
            final Integer _tmpMois;
            if (_cursor.isNull(_cursorIndexOfMois)) {
              _tmpMois = null;
            } else {
              _tmpMois = _cursor.getInt(_cursorIndexOfMois);
            }
            final String _tmpCategorie;
            if (_cursor.isNull(_cursorIndexOfCategorie)) {
              _tmpCategorie = null;
            } else {
              _tmpCategorie = _cursor.getString(_cursorIndexOfCategorie);
            }
            final String _tmpSuperCategorie;
            if (_cursor.isNull(_cursorIndexOfSuperCategorie)) {
              _tmpSuperCategorie = null;
            } else {
              _tmpSuperCategorie = _cursor.getString(_cursorIndexOfSuperCategorie);
            }
            final String _tmpMateriau;
            if (_cursor.isNull(_cursorIndexOfMateriau)) {
              _tmpMateriau = null;
            } else {
              _tmpMateriau = _cursor.getString(_cursorIndexOfMateriau);
            }
            final String _tmpTirage;
            if (_cursor.isNull(_cursorIndexOfTirage)) {
              _tmpTirage = null;
            } else {
              _tmpTirage = _cursor.getString(_cursorIndexOfTirage);
            }
            final String _tmpDimensions;
            if (_cursor.isNull(_cursorIndexOfDimensions)) {
              _tmpDimensions = null;
            } else {
              _tmpDimensions = _cursor.getString(_cursorIndexOfDimensions);
            }
            final Double _tmpPrixAchat;
            if (_cursor.isNull(_cursorIndexOfPrixAchat)) {
              _tmpPrixAchat = null;
            } else {
              _tmpPrixAchat = _cursor.getDouble(_cursorIndexOfPrixAchat);
            }
            final Double _tmpValeurEstimee;
            if (_cursor.isNull(_cursorIndexOfValeurEstimee)) {
              _tmpValeurEstimee = null;
            } else {
              _tmpValeurEstimee = _cursor.getDouble(_cursorIndexOfValeurEstimee);
            }
            final String _tmpLieuAchat;
            if (_cursor.isNull(_cursorIndexOfLieuAchat)) {
              _tmpLieuAchat = null;
            } else {
              _tmpLieuAchat = _cursor.getString(_cursorIndexOfLieuAchat);
            }
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final String _tmpImageUri;
            if (_cursor.isNull(_cursorIndexOfImageUri)) {
              _tmpImageUri = null;
            } else {
              _tmpImageUri = _cursor.getString(_cursorIndexOfImageUri);
            }
            final String _tmpLocalisation;
            if (_cursor.isNull(_cursorIndexOfLocalisation)) {
              _tmpLocalisation = null;
            } else {
              _tmpLocalisation = _cursor.getString(_cursorIndexOfLocalisation);
            }
            final byte[] _tmpImageEmbedding;
            if (_cursor.isNull(_cursorIndexOfImageEmbedding)) {
              _tmpImageEmbedding = null;
            } else {
              _tmpImageEmbedding = _cursor.getBlob(_cursorIndexOfImageEmbedding);
            }
            final boolean _tmpIsPossessed;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsPossessed);
            _tmpIsPossessed = _tmp != 0;
            _item = new CollectionItem(_tmpId,_tmpRemoteId,_tmpTitre,_tmpEditeur,_tmpAnnee,_tmpMois,_tmpCategorie,_tmpSuperCategorie,_tmpMateriau,_tmpTirage,_tmpDimensions,_tmpPrixAchat,_tmpValeurEstimee,_tmpLieuAchat,_tmpDescription,_tmpImageUri,_tmpLocalisation,_tmpImageEmbedding,_tmpIsPossessed);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public LiveData<List<CollectionItem>> advancedSearch(final String titre, final String editeur,
      final Integer annee, final Integer mois, final String superCategorie, final String categorie,
      final String description) {
    final String _sql = "\n"
            + "        SELECT * FROM collection_items WHERE \n"
            + "            (? IS NULL OR titre LIKE ?) AND\n"
            + "            (? IS NULL OR editeur LIKE ?) AND\n"
            + "            (? IS NULL OR annee = ?) AND\n"
            + "            (? IS NULL OR mois = ?) AND\n"
            + "            (? IS NULL OR superCategorie = ?) AND\n"
            + "            (? IS NULL OR categorie LIKE ?) AND\n"
            + "            (? IS NULL OR description LIKE ?)\n"
            + "        ORDER BY annee DESC, mois DESC\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 14);
    int _argIndex = 1;
    if (titre == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, titre);
    }
    _argIndex = 2;
    if (titre == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, titre);
    }
    _argIndex = 3;
    if (editeur == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, editeur);
    }
    _argIndex = 4;
    if (editeur == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, editeur);
    }
    _argIndex = 5;
    if (annee == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindLong(_argIndex, annee);
    }
    _argIndex = 6;
    if (annee == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindLong(_argIndex, annee);
    }
    _argIndex = 7;
    if (mois == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindLong(_argIndex, mois);
    }
    _argIndex = 8;
    if (mois == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindLong(_argIndex, mois);
    }
    _argIndex = 9;
    if (superCategorie == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, superCategorie);
    }
    _argIndex = 10;
    if (superCategorie == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, superCategorie);
    }
    _argIndex = 11;
    if (categorie == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, categorie);
    }
    _argIndex = 12;
    if (categorie == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, categorie);
    }
    _argIndex = 13;
    if (description == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, description);
    }
    _argIndex = 14;
    if (description == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, description);
    }
    return __db.getInvalidationTracker().createLiveData(new String[] {"collection_items"}, false, new Callable<List<CollectionItem>>() {
      @Override
      @Nullable
      public List<CollectionItem> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfRemoteId = CursorUtil.getColumnIndexOrThrow(_cursor, "remoteId");
          final int _cursorIndexOfTitre = CursorUtil.getColumnIndexOrThrow(_cursor, "titre");
          final int _cursorIndexOfEditeur = CursorUtil.getColumnIndexOrThrow(_cursor, "editeur");
          final int _cursorIndexOfAnnee = CursorUtil.getColumnIndexOrThrow(_cursor, "annee");
          final int _cursorIndexOfMois = CursorUtil.getColumnIndexOrThrow(_cursor, "mois");
          final int _cursorIndexOfCategorie = CursorUtil.getColumnIndexOrThrow(_cursor, "categorie");
          final int _cursorIndexOfSuperCategorie = CursorUtil.getColumnIndexOrThrow(_cursor, "superCategorie");
          final int _cursorIndexOfMateriau = CursorUtil.getColumnIndexOrThrow(_cursor, "materiau");
          final int _cursorIndexOfTirage = CursorUtil.getColumnIndexOrThrow(_cursor, "tirage");
          final int _cursorIndexOfDimensions = CursorUtil.getColumnIndexOrThrow(_cursor, "dimensions");
          final int _cursorIndexOfPrixAchat = CursorUtil.getColumnIndexOrThrow(_cursor, "prixAchat");
          final int _cursorIndexOfValeurEstimee = CursorUtil.getColumnIndexOrThrow(_cursor, "valeurEstimee");
          final int _cursorIndexOfLieuAchat = CursorUtil.getColumnIndexOrThrow(_cursor, "lieuAchat");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfImageUri = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUri");
          final int _cursorIndexOfLocalisation = CursorUtil.getColumnIndexOrThrow(_cursor, "localisation");
          final int _cursorIndexOfImageEmbedding = CursorUtil.getColumnIndexOrThrow(_cursor, "imageEmbedding");
          final int _cursorIndexOfIsPossessed = CursorUtil.getColumnIndexOrThrow(_cursor, "isPossessed");
          final List<CollectionItem> _result = new ArrayList<CollectionItem>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CollectionItem _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final Integer _tmpRemoteId;
            if (_cursor.isNull(_cursorIndexOfRemoteId)) {
              _tmpRemoteId = null;
            } else {
              _tmpRemoteId = _cursor.getInt(_cursorIndexOfRemoteId);
            }
            final String _tmpTitre;
            _tmpTitre = _cursor.getString(_cursorIndexOfTitre);
            final String _tmpEditeur;
            if (_cursor.isNull(_cursorIndexOfEditeur)) {
              _tmpEditeur = null;
            } else {
              _tmpEditeur = _cursor.getString(_cursorIndexOfEditeur);
            }
            final Integer _tmpAnnee;
            if (_cursor.isNull(_cursorIndexOfAnnee)) {
              _tmpAnnee = null;
            } else {
              _tmpAnnee = _cursor.getInt(_cursorIndexOfAnnee);
            }
            final Integer _tmpMois;
            if (_cursor.isNull(_cursorIndexOfMois)) {
              _tmpMois = null;
            } else {
              _tmpMois = _cursor.getInt(_cursorIndexOfMois);
            }
            final String _tmpCategorie;
            if (_cursor.isNull(_cursorIndexOfCategorie)) {
              _tmpCategorie = null;
            } else {
              _tmpCategorie = _cursor.getString(_cursorIndexOfCategorie);
            }
            final String _tmpSuperCategorie;
            if (_cursor.isNull(_cursorIndexOfSuperCategorie)) {
              _tmpSuperCategorie = null;
            } else {
              _tmpSuperCategorie = _cursor.getString(_cursorIndexOfSuperCategorie);
            }
            final String _tmpMateriau;
            if (_cursor.isNull(_cursorIndexOfMateriau)) {
              _tmpMateriau = null;
            } else {
              _tmpMateriau = _cursor.getString(_cursorIndexOfMateriau);
            }
            final String _tmpTirage;
            if (_cursor.isNull(_cursorIndexOfTirage)) {
              _tmpTirage = null;
            } else {
              _tmpTirage = _cursor.getString(_cursorIndexOfTirage);
            }
            final String _tmpDimensions;
            if (_cursor.isNull(_cursorIndexOfDimensions)) {
              _tmpDimensions = null;
            } else {
              _tmpDimensions = _cursor.getString(_cursorIndexOfDimensions);
            }
            final Double _tmpPrixAchat;
            if (_cursor.isNull(_cursorIndexOfPrixAchat)) {
              _tmpPrixAchat = null;
            } else {
              _tmpPrixAchat = _cursor.getDouble(_cursorIndexOfPrixAchat);
            }
            final Double _tmpValeurEstimee;
            if (_cursor.isNull(_cursorIndexOfValeurEstimee)) {
              _tmpValeurEstimee = null;
            } else {
              _tmpValeurEstimee = _cursor.getDouble(_cursorIndexOfValeurEstimee);
            }
            final String _tmpLieuAchat;
            if (_cursor.isNull(_cursorIndexOfLieuAchat)) {
              _tmpLieuAchat = null;
            } else {
              _tmpLieuAchat = _cursor.getString(_cursorIndexOfLieuAchat);
            }
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final String _tmpImageUri;
            if (_cursor.isNull(_cursorIndexOfImageUri)) {
              _tmpImageUri = null;
            } else {
              _tmpImageUri = _cursor.getString(_cursorIndexOfImageUri);
            }
            final String _tmpLocalisation;
            if (_cursor.isNull(_cursorIndexOfLocalisation)) {
              _tmpLocalisation = null;
            } else {
              _tmpLocalisation = _cursor.getString(_cursorIndexOfLocalisation);
            }
            final byte[] _tmpImageEmbedding;
            if (_cursor.isNull(_cursorIndexOfImageEmbedding)) {
              _tmpImageEmbedding = null;
            } else {
              _tmpImageEmbedding = _cursor.getBlob(_cursorIndexOfImageEmbedding);
            }
            final boolean _tmpIsPossessed;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsPossessed);
            _tmpIsPossessed = _tmp != 0;
            _item = new CollectionItem(_tmpId,_tmpRemoteId,_tmpTitre,_tmpEditeur,_tmpAnnee,_tmpMois,_tmpCategorie,_tmpSuperCategorie,_tmpMateriau,_tmpTirage,_tmpDimensions,_tmpPrixAchat,_tmpValeurEstimee,_tmpLieuAchat,_tmpDescription,_tmpImageUri,_tmpLocalisation,_tmpImageEmbedding,_tmpIsPossessed);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public LiveData<List<CategoryInfo>> getSuperCategoryInfo(final boolean isPossessed) {
    final String _sql = "SELECT superCategorie as name, COUNT(*) as count FROM collection_items WHERE isPossessed = ? AND superCategorie IS NOT NULL AND superCategorie != '' GROUP BY superCategorie ORDER BY superCategorie ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    final int _tmp = isPossessed ? 1 : 0;
    _statement.bindLong(_argIndex, _tmp);
    return __db.getInvalidationTracker().createLiveData(new String[] {"collection_items"}, false, new Callable<List<CategoryInfo>>() {
      @Override
      @Nullable
      public List<CategoryInfo> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfName = 0;
          final int _cursorIndexOfCount = 1;
          final List<CategoryInfo> _result = new ArrayList<CategoryInfo>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CategoryInfo _item;
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final int _tmpCount;
            _tmpCount = _cursor.getInt(_cursorIndexOfCount);
            _item = new CategoryInfo(_tmpName,_tmpCount);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public LiveData<List<CategoryInfo>> getCategoryInfoForSuperCategory(final String superCategory,
      final boolean isPossessed) {
    final String _sql = "SELECT categorie as name, COUNT(*) as count FROM collection_items WHERE superCategorie = ? AND isPossessed = ? AND categorie IS NOT NULL AND categorie != '' GROUP BY categorie ORDER BY categorie ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, superCategory);
    _argIndex = 2;
    final int _tmp = isPossessed ? 1 : 0;
    _statement.bindLong(_argIndex, _tmp);
    return __db.getInvalidationTracker().createLiveData(new String[] {"collection_items"}, false, new Callable<List<CategoryInfo>>() {
      @Override
      @Nullable
      public List<CategoryInfo> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfName = 0;
          final int _cursorIndexOfCount = 1;
          final List<CategoryInfo> _result = new ArrayList<CategoryInfo>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CategoryInfo _item;
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final int _tmpCount;
            _tmpCount = _cursor.getInt(_cursorIndexOfCount);
            _item = new CategoryInfo(_tmpName,_tmpCount);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public LiveData<List<CollectionItem>> getItemsBySuperCategoryAndCategory(
      final String superCategory, final String category, final boolean isPossessed) {
    final String _sql = "SELECT * FROM collection_items WHERE superCategorie = ? AND categorie = ? AND isPossessed = ? ORDER BY titre ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindString(_argIndex, superCategory);
    _argIndex = 2;
    _statement.bindString(_argIndex, category);
    _argIndex = 3;
    final int _tmp = isPossessed ? 1 : 0;
    _statement.bindLong(_argIndex, _tmp);
    return __db.getInvalidationTracker().createLiveData(new String[] {"collection_items"}, false, new Callable<List<CollectionItem>>() {
      @Override
      @Nullable
      public List<CollectionItem> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfRemoteId = CursorUtil.getColumnIndexOrThrow(_cursor, "remoteId");
          final int _cursorIndexOfTitre = CursorUtil.getColumnIndexOrThrow(_cursor, "titre");
          final int _cursorIndexOfEditeur = CursorUtil.getColumnIndexOrThrow(_cursor, "editeur");
          final int _cursorIndexOfAnnee = CursorUtil.getColumnIndexOrThrow(_cursor, "annee");
          final int _cursorIndexOfMois = CursorUtil.getColumnIndexOrThrow(_cursor, "mois");
          final int _cursorIndexOfCategorie = CursorUtil.getColumnIndexOrThrow(_cursor, "categorie");
          final int _cursorIndexOfSuperCategorie = CursorUtil.getColumnIndexOrThrow(_cursor, "superCategorie");
          final int _cursorIndexOfMateriau = CursorUtil.getColumnIndexOrThrow(_cursor, "materiau");
          final int _cursorIndexOfTirage = CursorUtil.getColumnIndexOrThrow(_cursor, "tirage");
          final int _cursorIndexOfDimensions = CursorUtil.getColumnIndexOrThrow(_cursor, "dimensions");
          final int _cursorIndexOfPrixAchat = CursorUtil.getColumnIndexOrThrow(_cursor, "prixAchat");
          final int _cursorIndexOfValeurEstimee = CursorUtil.getColumnIndexOrThrow(_cursor, "valeurEstimee");
          final int _cursorIndexOfLieuAchat = CursorUtil.getColumnIndexOrThrow(_cursor, "lieuAchat");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfImageUri = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUri");
          final int _cursorIndexOfLocalisation = CursorUtil.getColumnIndexOrThrow(_cursor, "localisation");
          final int _cursorIndexOfImageEmbedding = CursorUtil.getColumnIndexOrThrow(_cursor, "imageEmbedding");
          final int _cursorIndexOfIsPossessed = CursorUtil.getColumnIndexOrThrow(_cursor, "isPossessed");
          final List<CollectionItem> _result = new ArrayList<CollectionItem>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CollectionItem _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final Integer _tmpRemoteId;
            if (_cursor.isNull(_cursorIndexOfRemoteId)) {
              _tmpRemoteId = null;
            } else {
              _tmpRemoteId = _cursor.getInt(_cursorIndexOfRemoteId);
            }
            final String _tmpTitre;
            _tmpTitre = _cursor.getString(_cursorIndexOfTitre);
            final String _tmpEditeur;
            if (_cursor.isNull(_cursorIndexOfEditeur)) {
              _tmpEditeur = null;
            } else {
              _tmpEditeur = _cursor.getString(_cursorIndexOfEditeur);
            }
            final Integer _tmpAnnee;
            if (_cursor.isNull(_cursorIndexOfAnnee)) {
              _tmpAnnee = null;
            } else {
              _tmpAnnee = _cursor.getInt(_cursorIndexOfAnnee);
            }
            final Integer _tmpMois;
            if (_cursor.isNull(_cursorIndexOfMois)) {
              _tmpMois = null;
            } else {
              _tmpMois = _cursor.getInt(_cursorIndexOfMois);
            }
            final String _tmpCategorie;
            if (_cursor.isNull(_cursorIndexOfCategorie)) {
              _tmpCategorie = null;
            } else {
              _tmpCategorie = _cursor.getString(_cursorIndexOfCategorie);
            }
            final String _tmpSuperCategorie;
            if (_cursor.isNull(_cursorIndexOfSuperCategorie)) {
              _tmpSuperCategorie = null;
            } else {
              _tmpSuperCategorie = _cursor.getString(_cursorIndexOfSuperCategorie);
            }
            final String _tmpMateriau;
            if (_cursor.isNull(_cursorIndexOfMateriau)) {
              _tmpMateriau = null;
            } else {
              _tmpMateriau = _cursor.getString(_cursorIndexOfMateriau);
            }
            final String _tmpTirage;
            if (_cursor.isNull(_cursorIndexOfTirage)) {
              _tmpTirage = null;
            } else {
              _tmpTirage = _cursor.getString(_cursorIndexOfTirage);
            }
            final String _tmpDimensions;
            if (_cursor.isNull(_cursorIndexOfDimensions)) {
              _tmpDimensions = null;
            } else {
              _tmpDimensions = _cursor.getString(_cursorIndexOfDimensions);
            }
            final Double _tmpPrixAchat;
            if (_cursor.isNull(_cursorIndexOfPrixAchat)) {
              _tmpPrixAchat = null;
            } else {
              _tmpPrixAchat = _cursor.getDouble(_cursorIndexOfPrixAchat);
            }
            final Double _tmpValeurEstimee;
            if (_cursor.isNull(_cursorIndexOfValeurEstimee)) {
              _tmpValeurEstimee = null;
            } else {
              _tmpValeurEstimee = _cursor.getDouble(_cursorIndexOfValeurEstimee);
            }
            final String _tmpLieuAchat;
            if (_cursor.isNull(_cursorIndexOfLieuAchat)) {
              _tmpLieuAchat = null;
            } else {
              _tmpLieuAchat = _cursor.getString(_cursorIndexOfLieuAchat);
            }
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final String _tmpImageUri;
            if (_cursor.isNull(_cursorIndexOfImageUri)) {
              _tmpImageUri = null;
            } else {
              _tmpImageUri = _cursor.getString(_cursorIndexOfImageUri);
            }
            final String _tmpLocalisation;
            if (_cursor.isNull(_cursorIndexOfLocalisation)) {
              _tmpLocalisation = null;
            } else {
              _tmpLocalisation = _cursor.getString(_cursorIndexOfLocalisation);
            }
            final byte[] _tmpImageEmbedding;
            if (_cursor.isNull(_cursorIndexOfImageEmbedding)) {
              _tmpImageEmbedding = null;
            } else {
              _tmpImageEmbedding = _cursor.getBlob(_cursorIndexOfImageEmbedding);
            }
            final boolean _tmpIsPossessed;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsPossessed);
            _tmpIsPossessed = _tmp_1 != 0;
            _item = new CollectionItem(_tmpId,_tmpRemoteId,_tmpTitre,_tmpEditeur,_tmpAnnee,_tmpMois,_tmpCategorie,_tmpSuperCategorie,_tmpMateriau,_tmpTirage,_tmpDimensions,_tmpPrixAchat,_tmpValeurEstimee,_tmpLieuAchat,_tmpDescription,_tmpImageUri,_tmpLocalisation,_tmpImageEmbedding,_tmpIsPossessed);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
