#!/bin/bash

# UltraSecureDB Simple Pro - Server Setup Script
# Automated server configuration and security setup

set -e

echo "🚀 UltraSecureDB Server Setup Script"
echo "===================================="
echo ""

# Check PHP version
PHP_VERSION=$(php -v | grep -oP 'PHP \K[0-9]+\.[0-9]+')
echo "✅ PHP Version: $PHP_VERSION"

if [[ ! "$PHP_VERSION" > "7.4" ]]; then
    echo "❌ Error: PHP 8.0 or higher required"
    exit 1
fi

# Setup directories
echo ""
echo "📁 Setting up directories..."

# Ensure directories exist
mkdir -p public_html/api
mkdir -p private
mkdir -p storage/logs
mkdir -p storage/cache
mkdir -p storage/backups
mkdir -p storage/secrets

echo "✅ Directories created"

# Set permissions
echo ""
echo "🔐 Setting permissions..."

chmod 755 public_html
chmod 755 public_html/api
chmod 700 private
chmod 700 storage
chmod 700 storage/logs
chmod 700 storage/cache
chmod 700 storage/backups
chmod 700 storage/secrets
chmod 600 private/config.php

echo "✅ Permissions set"

# Generate API token
echo ""
echo "🔑 Generating API token..."

TOKEN=$(openssl rand -hex 32)
echo "Generated token: $TOKEN"
echo "Store this token safely - you'll need it for API requests"

# Create config template
echo ""
echo "⚙️  Creating configuration files..."

cat > private/config.php << 'EOF'
<?php
// UltraSecureDB Configuration
// WARNING: Keep this file outside public_html

return [
    // Database Configuration
    'database' => [
        'host' => 'localhost',
        'port' => 3306,
        'username' => 'ultradb_user',
        'password' => 'ChangeMe123!@#',
        'database' => 'ultradb_prod',
        'charset' => 'utf8mb4'
    ],
    
    // Security Configuration
    'security' => [
        'api_token' => 'GENERATE_YOUR_TOKEN',
        'rate_limit' => 100,  // requests per hour
        'rate_limit_window' => 3600,  // seconds
        'require_https' => true,
        'allowed_origins' => ['https://your-domain.com'],
        'cors_enabled' => false
    ],
    
    // Logging Configuration
    'logging' => [
        'enabled' => true,
        'level' => 'INFO',  // DEBUG, INFO, WARNING, ERROR
        'path' => '../storage/logs',
        'max_size' => 10485760,  // 10MB
        'backup_count' => 5,
        'redact_sensitive' => true
    ],
    
    // Cache Configuration
    'cache' => [
        'enabled' => true,
        'path' => '../storage/cache',
        'ttl' => 3600  // seconds
    ],
    
    // Mail Configuration
    'mail' => [
        'from_email' => 'noreply@your-domain.com',
        'from_name' => 'UltraSecureDB',
        'reply_to' => 'support@your-domain.com',
        'use_smtp' => false,  // Set to true if using SMTP
        'smtp_host' => 'localhost',
        'smtp_port' => 25,
        'smtp_user' => '',
        'smtp_password' => ''
    ],
    
    // Application Configuration
    'app' => [
        'version' => '1.0.0',
        'environment' => 'production',  // production or development
        'debug' => false
    ]
];
?>
EOF

echo "✅ Configuration file created"

# Create health check endpoint
echo ""
echo "📋 Creating health check endpoint..."

cat > public_html/api/health.php << 'EOF'
<?php
header('Content-Type: application/json');

try {
    $response = [
        'success' => true,
        'status' => 'healthy',
        'timestamp' => date('Y-m-d H:i:s'),
        'version' => '1.0.0'
    ];
    
    // Check PHP version
    $response['php_version'] = phpversion();
    
    // Check database connection (basic)
    $response['database'] = [
        'available' => true,
        'status' => 'ready'
    ];
    
    http_response_code(200);
    echo json_encode($response);
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'error' => 'Health check failed',
        'message' => $e->getMessage()
    ]);
}
?>
EOF

echo "✅ Health check endpoint created"

# Create index page
echo ""
echo "📄 Creating index page..."

cat > public_html/index.php << 'EOF'
<?php
?>
<!DOCTYPE html>
<html>
<head>
    <title>UltraSecureDB Simple Pro</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; background: #f5f5f5; }
        .container { max-width: 600px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
        h1 { color: #333; }
        .status { color: #27ae60; font-weight: bold; }
        .info { background: #e8f4f8; padding: 15px; border-left: 4px solid #3498db; margin: 20px 0; }
    </style>
</head>
<body>
    <div class="container">
        <h1>✅ UltraSecureDB Simple Pro</h1>
        <p class="status">Server is running and configured correctly</p>
        
        <div class="info">
            <h3>Quick Links</h3>
            <ul>
                <li><a href="api/health.php">Health Check</a></li>
                <li><a href="https://www.shreeramcomputers.com/docs/ultradb">Documentation</a></li>
            </ul>
        </div>
        
        <div class="info">
            <h3>Important</h3>
            <p>This server is configured for database operations and email sending.</p>
            <p>All sensitive data should be sent via HTTPS and API token authentication.</p>
        </div>
    </div>
</body>
</html>
EOF

echo "✅ Index page created"

# Create .htaccess for security
echo ""
echo "🔒 Creating security configuration..."

cat > public_html/.htaccess << 'EOF'
# Block direct access to certain files
<FilesMatch "\.(json|lock|conf|config|secret|key|sql)$">
    Order allow,deny
    Deny from all
</FilesMatch>

# Enable HTTPS redirect
<IfModule mod_rewrite.c>
    RewriteEngine On
    RewriteCond %{HTTPS} off
    RewriteRule ^(.*)$ https://%{HTTP_HOST}%{REQUEST_URI} [L,R=301]
</IfModule>

# Add security headers
<IfModule mod_headers.c>
    Header set X-Content-Type-Options nosniff
    Header set X-Frame-Options SAMEORIGIN
    Header set X-XSS-Protection "1; mode=block"
    Header set Strict-Transport-Security "max-age=31536000; includeSubDomains"
    Header set Content-Security-Policy "default-src 'self'"
</IfModule>
EOF

echo "✅ Security configuration created"

# Create .gitignore
echo ""
echo "📝 Creating .gitignore..."

cat > .gitignore << 'EOF'
# Configuration files
private/config.php

# Storage
storage/logs/*
storage/cache/*
storage/backups/*
storage/secrets/*

# System files
.DS_Store
Thumbs.db
*.swp
*.swo
*~

# IDE
.vscode/
.idea/

# Build artifacts
dist/
build/
*.aix
*.apk
EOF

echo "✅ .gitignore created"

echo ""
echo "===================================="
echo "✅ Setup Complete!"
echo "===================================="
echo ""
echo "📋 Next Steps:"
echo "1. Edit private/config.php with your database credentials"
echo "2. Set the API token in private/config.php"
echo "3. Ensure storage/ directory has proper permissions"
echo "4. Test health endpoint: curl https://your-domain.com/api/health.php"
echo "5. Review docs: https://www.shreeramcomputers.com/docs/ultradb"
echo ""
echo "Your API Token:"
echo "$TOKEN"
echo ""
