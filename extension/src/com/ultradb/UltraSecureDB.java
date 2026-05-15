package com.ultradb;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.util.YailList;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@DesignerComponent(
    version = 1,
    description = "UltraSecureDB Simple Pro - Production-ready MySQL extension",
    category = ComponentCategory.EXTENSION,
    nonVisible = true,
    iconName = "images/extension.png"
)
@SimpleObject(external = true)
public class UltraSecureDB extends AndroidNonvisibleComponent {

  private static final String TAG = "UltraSecureDB";
  private Activity activity;
  private DatabaseManager dbManager;
  private ConfigManager configManager;
  private CryptoManager cryptoManager;
  private MailManager mailManager;
  private ExecutorService executor;
  private Handler mainHandler;

  public UltraSecureDB(ComponentContainer container) {
    super(container.$context());
    this.activity = container.$context();
    this.executor = Executors.newFixedThreadPool(4);
    this.mainHandler = new Handler(Looper.getMainLooper());
    this.dbManager = new DatabaseManager();
    this.configManager = new ConfigManager();
    this.cryptoManager = new CryptoManager();
    this.mailManager = new MailManager();
  }

  // ============ INITIALIZATION & CONNECTION ============

  @SimpleFunction(description = "Initialize with server URL")
  public void Initialize(String serverUrl) {
    executor.execute(() -> {
      try {
        configManager.setServerUrl(serverUrl);
        mainHandler.post(() -> OnInitialized());
      } catch (Exception e) {
        mainHandler.post(() -> OnError("Initialize", e.getMessage()));
      }
    });
  }

  @SimpleFunction(description = "Connect to database with credentials")
  public void Connect(String host, String username, String password, String database) {
    executor.execute(() -> {
      try {
        dbManager.connect(host, username, password, database);
        mainHandler.post(() -> OnConnected());
      } catch (Exception e) {
        mainHandler.post(() -> OnError("Connect", e.getMessage()));
      }
    });
  }

  @SimpleFunction(description = "Disconnect from database")
  public void Disconnect() {
    executor.execute(() -> {
      try {
        dbManager.disconnect();
        mainHandler.post(() -> OnDisconnected());
      } catch (Exception e) {
        mainHandler.post(() -> OnError("Disconnect", e.getMessage()));
      }
    });
  }

  @SimpleFunction(description = "Test connection to database")
  public void TestConnection() {
    executor.execute(() -> {
      try {
        boolean connected = dbManager.testConnection();
        mainHandler.post(() -> OnConnectionTest(connected));
      } catch (Exception e) {
        mainHandler.post(() -> OnError("TestConnection", e.getMessage()));
      }
    });
  }

  @SimpleFunction(description = "Get connection status")
  public boolean GetStatus() {
    return dbManager.isConnected();
  }

  @SimpleFunction(description = "Perform health check on connection")
  public void HealthCheck() {
    executor.execute(() -> {
      try {
        JSONObject health = dbManager.performHealthCheck();
        mainHandler.post(() -> OnHealthCheck(health.toString()));
      } catch (Exception e) {
        mainHandler.post(() -> OnError("HealthCheck", e.getMessage()));
      }
    });
  }

  @SimpleFunction(description = "Enable auto-reconnect with timeout")
  public void SetAutoReconnect(boolean enable, int timeoutSeconds) {
    dbManager.setAutoReconnect(enable, timeoutSeconds);
  }

  // ============ QUERY OPERATIONS ============

  @SimpleFunction(description = "Execute query and return JSON result")
  public void QueryJSON(String query) {
    if (!validateQuery(query)) {
      mainHandler.post(() -> OnError("QueryJSON", "Invalid query"));
      return;
    }
    
    executor.execute(() -> {
      try {
        JSONObject result = dbManager.executeQueryJSON(query);
        mainHandler.post(() -> OnQueryResult(result.toString()));
      } catch (Exception e) {
        mainHandler.post(() -> OnError("QueryJSON", e.getMessage()));
      }
    });
  }

  @SimpleFunction(description = "Execute query and return list result")
  public void Query(String query) {
    if (!validateQuery(query)) {
      mainHandler.post(() -> OnError("Query", "Invalid query"));
      return;
    }
    
    executor.execute(() -> {
      try {
        JSONArray result = dbManager.executeQuery(query);
        YailList yailList = QueryExecutor.jsonArrayToYailList(result);
        mainHandler.post(() -> OnQueryListResult(yailList));
      } catch (Exception e) {
        mainHandler.post(() -> OnError("Query", e.getMessage()));
      }
    });
  }

  @SimpleFunction(description = "Execute insert/update/delete query")
  public void ExecuteUpdate(String query) {
    if (!validateQuery(query)) {
      mainHandler.post(() -> OnError("ExecuteUpdate", "Invalid query"));
      return;
    }
    
    executor.execute(() -> {
      try {
        int rowCount = dbManager.executeUpdate(query);
        mainHandler.post(() -> OnUpdateComplete(rowCount));
      } catch (Exception e) {
        mainHandler.post(() -> OnError("ExecuteUpdate", e.getMessage()));
      }
    });
  }

  @SimpleFunction(description = "Execute batch of queries")
  public void BatchExecute(YailList queries) {
    executor.execute(() -> {
      try {
        int rowsAffected = dbManager.executeBatch(queries);
        mainHandler.post(() -> OnBatchComplete(rowsAffected));
      } catch (Exception e) {
        mainHandler.post(() -> OnError("BatchExecute", e.getMessage()));
      }
    });
  }

  @SimpleFunction(description = "Get row count from result")
  public int GetRowCount() {
    return dbManager.getLastRowCount();
  }

  @SimpleFunction(description = "Get column information")
  public String GetColumnInfo() {
    try {
      JSONArray columnInfo = dbManager.getColumnInfo();
      return columnInfo.toString();
    } catch (Exception e) {
      OnError("GetColumnInfo", e.getMessage());
      return "[]";
    }
  }

  // ============ TRANSACTION OPERATIONS ============

  @SimpleFunction(description = "Begin database transaction")
  public void BeginTransaction() {
    executor.execute(() -> {
      try {
        dbManager.beginTransaction();
        mainHandler.post(() -> OnTransactionBegin());
      } catch (Exception e) {
        mainHandler.post(() -> OnError("BeginTransaction", e.getMessage()));
      }
    });
  }

  @SimpleFunction(description = "Commit transaction")
  public void Commit() {
    executor.execute(() -> {
      try {
        dbManager.commit();
        mainHandler.post(() -> OnTransactionCommit());
      } catch (Exception e) {
        mainHandler.post(() -> OnError("Commit", e.getMessage()));
      }
    });
  }

  @SimpleFunction(description = "Rollback transaction")
  public void Rollback() {
    executor.execute(() -> {
      try {
        dbManager.rollback();
        mainHandler.post(() -> OnTransactionRollback());
      } catch (Exception e) {
        mainHandler.post(() -> OnError("Rollback", e.getMessage()));
      }
    });
  }

  // ============ ENCRYPTION & HASHING ============

  @SimpleFunction(description = "Encrypt value")
  public String EncryptValue(String value) {
    try {
      return cryptoManager.encrypt(value);
    } catch (Exception e) {
      OnError("EncryptValue", e.getMessage());
      return "";
    }
  }

  @SimpleFunction(description = "Decrypt value")
  public String DecryptValue(String encryptedValue) {
    try {
      return cryptoManager.decrypt(encryptedValue);
    } catch (Exception e) {
      OnError("DecryptValue", e.getMessage());
      return "";
    }
  }

  @SimpleFunction(description = "Hash value with salt")
  public String HashValue(String value) {
    try {
      return cryptoManager.hash(value);
    } catch (Exception e) {
      OnError("HashValue", e.getMessage());
      return "";
    }
  }

  @SimpleFunction(description = "Verify hash")
  public boolean VerifyHash(String value, String hash) {
    try {
      return cryptoManager.verifyHash(value, hash);
    } catch (Exception e) {
      OnError("VerifyHash", e.getMessage());
      return false;
    }
  }

  // ============ MAIL OPERATIONS ============

  @SimpleFunction(description = "Send basic email")
  public void SendMail(String to, String subject, String message) {
    executor.execute(() -> {
      try {
        mailManager.sendMail(to, subject, message, "", false);
        mainHandler.post(() -> OnMailSent());
      } catch (Exception e) {
        mainHandler.post(() -> OnError("SendMail", e.getMessage()));
      }
    });
  }

  @SimpleFunction(description = "Send email with from name and reply-to")
  public void SendMailAdvanced(String to, String subject, String message, 
                               String fromName, String replyTo, boolean isHtml) {
    executor.execute(() -> {
      try {
        mailManager.sendMailAdvanced(to, subject, message, fromName, replyTo, isHtml);
        mainHandler.post(() -> OnMailSent());
      } catch (Exception e) {
        mainHandler.post(() -> OnError("SendMailAdvanced", e.getMessage()));
      }
    });
  }

  @SimpleFunction(description = "Send email with CC and BCC")
  public void SendMailWithCcBcc(String to, String cc, String bcc, 
                                String subject, String message, boolean isHtml) {
    executor.execute(() -> {
      try {
        mailManager.sendMailWithCcBcc(to, cc, bcc, subject, message, isHtml);
        mainHandler.post(() -> OnMailSent());
      } catch (Exception e) {
        mainHandler.post(() -> OnError("SendMailWithCcBcc", e.getMessage()));
      }
    });
  }

  // ============ CONFIG OPERATIONS ============

  @SimpleFunction(description = "Set secure configuration")
  public void SetSecureConfig(String key, String value) {
    try {
      configManager.setSecureConfig(key, cryptoManager.encrypt(value));
      OnConfigSet(key);
    } catch (Exception e) {
      OnError("SetSecureConfig", e.getMessage());
    }
  }

  @SimpleFunction(description = "Get secure configuration")
  public String GetSecureConfig(String key) {
    try {
      String encrypted = configManager.getSecureConfig(key);
      if (encrypted == null) return "";
      return cryptoManager.decrypt(encrypted);
    } catch (Exception e) {
      OnError("GetSecureConfig", e.getMessage());
      return "";
    }
  }

  @SimpleFunction(description = "Export configuration")
  public String ExportConfig() {
    try {
      return configManager.exportConfig().toString();
    } catch (Exception e) {
      OnError("ExportConfig", e.getMessage());
      return "{}";
    }
  }

  @SimpleFunction(description = "Import configuration")
  public void ImportConfig(String configJson) {
    try {
      configManager.importConfig(new JSONObject(configJson));
      OnConfigImported();
    } catch (Exception e) {
      OnError("ImportConfig", e.getMessage());
    }
  }

  @SimpleFunction(description = "Get extension version")
  public String GetVersion() {
    return "1.0.0";
  }

  // ============ UTILITY FUNCTIONS ============

  private boolean validateQuery(String query) {
    if (query == null || query.trim().isEmpty()) return false;
    return !query.trim().toLowerCase().matches("^(drop|alter|truncate).*");
  }

  // ============ EVENTS ============

  @SimpleEvent(description = "Called when extension is initialized")
  public void OnInitialized() {
    EventDispatcher.dispatchEvent(this, "OnInitialized");
  }

  @SimpleEvent(description = "Called when connected to database")
  public void OnConnected() {
    EventDispatcher.dispatchEvent(this, "OnConnected");
  }

  @SimpleEvent(description = "Called when disconnected from database")
  public void OnDisconnected() {
    EventDispatcher.dispatchEvent(this, "OnDisconnected");
  }

  @SimpleEvent(description = "Called with connection test result")
  public void OnConnectionTest(boolean success) {
    EventDispatcher.dispatchEvent(this, "OnConnectionTest", success);
  }

  @SimpleEvent(description = "Called with health check result")
  public void OnHealthCheck(String result) {
    EventDispatcher.dispatchEvent(this, "OnHealthCheck", result);
  }

  @SimpleEvent(description = "Called with query JSON result")
  public void OnQueryResult(String result) {
    EventDispatcher.dispatchEvent(this, "OnQueryResult", result);
  }

  @SimpleEvent(description = "Called with query list result")
  public void OnQueryListResult(YailList result) {
    EventDispatcher.dispatchEvent(this, "OnQueryListResult", result);
  }

  @SimpleEvent(description = "Called when update completes")
  public void OnUpdateComplete(int rowCount) {
    EventDispatcher.dispatchEvent(this, "OnUpdateComplete", rowCount);
  }

  @SimpleEvent(description = "Called when batch completes")
  public void OnBatchComplete(int rowsAffected) {
    EventDispatcher.dispatchEvent(this, "OnBatchComplete", rowsAffected);
  }

  @SimpleEvent(description = "Called when transaction begins")
  public void OnTransactionBegin() {
    EventDispatcher.dispatchEvent(this, "OnTransactionBegin");
  }

  @SimpleEvent(description = "Called when transaction commits")
  public void OnTransactionCommit() {
    EventDispatcher.dispatchEvent(this, "OnTransactionCommit");
  }

  @SimpleEvent(description = "Called when transaction rolls back")
  public void OnTransactionRollback() {
    EventDispatcher.dispatchEvent(this, "OnTransactionRollback");
  }

  @SimpleEvent(description = "Called when mail is sent")
  public void OnMailSent() {
    EventDispatcher.dispatchEvent(this, "OnMailSent");
  }

  @SimpleEvent(description = "Called on configuration set")
  public void OnConfigSet(String key) {
    EventDispatcher.dispatchEvent(this, "OnConfigSet", key);
  }

  @SimpleEvent(description = "Called when configuration is imported")
  public void OnConfigImported() {
    EventDispatcher.dispatchEvent(this, "OnConfigImported");
  }

  @SimpleEvent(description = "Called on error")
  public void OnError(String operation, String message) {
    EventDispatcher.dispatchEvent(this, "OnError", operation, message);
  }

  // ============ CLEANUP ============

  public void onDestroy() {
    try {
      dbManager.disconnect();
      executor.shutdown();
    } catch (Exception e) {
      LogManager.log("Error in onDestroy", e.getMessage());
    }
  }
}
