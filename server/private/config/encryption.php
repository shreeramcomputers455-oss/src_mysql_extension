<?php
/**
 * Encryption Configuration
 * KEEP IN private/ DIRECTORY
 */

return [
    'algorithm' => 'AES-256-CBC',
    
    // Encryption key - generate with: php -r "echo base64_encode(random_bytes(32));"
    // Store as environment variable or in .env file
    'key' => getenv('ENCRYPTION_KEY') ?: 'change-me-to-secure-random-key-base64-encoded',
    
    // Hash algorithm
    'hash_algo' => 'sha256',
    
    // Password hashing
    'password_algo' => PASSWORD_BCRYPT,
    'password_cost' => 12,
    
    // Enable encryption at rest
    'encrypt_at_rest' => true,
    'fields_to_encrypt' => [
        'password',
        'api_token',
        'secret_key',
    ],
];
?>
