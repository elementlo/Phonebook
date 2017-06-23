package com.weikuo.elemenzhang.phonebookwk.bean;

/**
 * Created by alvin on 16/9/13.
 */
public interface FlavorsConstants {

     String authorityDatabase = "mobi.infolife.appbackup.dao.provider.database";
     String authorityPreferences = "mobi.infolife.appbackup.dao.provider.Preferences";
     String authorityWifitransfer = "mobi.infolife.wifitransfer";


    interface BroadcastAction{
        String FILE_DB_CHANGED = "mobi.infolife.appbackup.file.db.changed";
        String FILE_DIR_CHANGED = "mobi.infolife.appbackup.file.dir.changed";
        String MOVE_FILE_FINISH = "mobi.infolife.appbackup.file.dir.moved";
        String MIGRATION_FINISH = "mobi.infolife.appbackup.file.dir.migrated";
        String ACTION_FILE_CHAGNE = "action_file_change";


        String UPDATE_PROGRESS = "mobi.infolife.appbackup.update.progress";
        String START_RECEIVE = "mobi.infolife.appbackup.start.receive";
        String FILE_COMPLETE = "mobi.infolife.appbackup.file.complete";
        String UPDATE_REALTIME_SPEED = "mobi.infolife.appbackup.update.realtime.speed";
        String START_RECEIVING_FILE = "mobi.infolife.appbackup.start.receiving.file";
        String START_RECEIVING_FILE_PROGRESS = "mobi.infolife.appbackup.receiving.file.progress";
        String RECEIVE_ONE_FILE_COMPLETE = "mobi.infolife.appbackup.receiving.one.file.complete";
        String ON_CONN_LOST = "mobi.infolife.appbackup.receiving.on.conn.lost";
        String IS_CANCELLED = "mobi.infolife.appbackup.is.cancelled";
        String ON_WRITE_ERROR = "mobi.infolife.appbackup.on.write.error";
        String ON_SENDER_RECEIVED = "mobi.infolife.appbackup.on.sender.received";
        String ON_FILE_INFOS_RECEIVED = "mobi.infolife.appbackup.on.fileinfos.received";
        String ON_FILE_ICONS_RECEIVED = "mobi.infolife.appbackup.on.icons.received";
        String ON_FILE_INFOS_CHANGED = "mobi.infolife.appbackup.on.fileinfos.changed";
        String AUTO_BACKUP_SUCCESS = "aobi.infolife.appbackup.autobackupsuccess";

    }

    interface Constants {
        String BASE_DIR_NAME = "App_Backup_Restore";
        String APP_PRIVATE_DIR = "/Android/data/mobi.infolife.appbackup";
        String PKG_NAME = "mobi.infolife.appbackup";
        String GOOGLE_URL = "http://bit.ly/2gXNM8P";
        String FACEBOOK_URL = "http://bit.ly/2h0QcUI";
    }
}
