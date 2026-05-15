package com.ultradb;

import org.json.JSONObject;
import org.json.JSONArray;
import java.util.HashMap;
import java.util.Map;

/**
 * ConfigManager - Encrypted Configuration Management
 * Stores and retrieves encrypted configuration values
 */
public class ConfigManager {

  private Map<String, String> config;
  private CryptoManager cryptoManager;
  private String serverUrl;

  public ConfigManager() {
    this.config = new HashMap<>();
    this.cryptoManager = new CryptoManager();
    this.serverUrl = "https://localhost";
  }

  /**
   * Set secure configuration value (encrypted)
   */
  public void setSecureConfig(String key, String encryptedValue) {
    if (key != null && !key.isEmpty() && encryptedValue != null) {
      config.put(key, encryptedValue);
    }
  }

  /**
   * Get secure configuration value (encrypted)
   */
  public String getSecureConfig(String key) {
    return config.get(key);
  }

  /**
   * Set server URL
   */
  public void setServerUrl(String url) {
    if (url != null && !url.isEmpty()) {
      this.serverUrl = url;
    }
  }

  /**
   * Get server URL
   */
  public String getServerUrl() {
    return serverUrl;
  }

  /**
   * Export all configuration as JSON
   */
  public JSONObject exportConfig() throws Exception {
    JSONObject exportData = new JSONObject();
    
    for (Map.Entry<String, String> entry : config.entrySet()) {
      exportData.put(entry.getKey(), entry.getValue());
    }
    
    return exportData;
  }

  /**
   * Import configuration from JSON
   */
  public void importConfig(JSONObject importData) throws Exception {
    if (importData == null) {
      return;
    }

    JSONArray keys = importData.names();
    if (keys != null) {
      for (int i = 0; i < keys.length(); i++) {
        String key = keys.getString(i);
        String value = importData.getString(key);
        config.put(key, value);
      }
    }
  }

  /**
   * Clear all configuration
   */
  public void clearConfig() {
    config.clear();
  }

  /**
   * Check if key exists
   */
  public boolean hasKey(String key) {
    return config.containsKey(key);
  }

  /**
   * Get configuration size
   */
  public int getConfigSize() {
    return config.size();
  }
}
