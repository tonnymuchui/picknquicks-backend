UPDATE users
SET enabled = true,
    email_verified = true,
    updated_at = NOW()
WHERE email = 'tonykanyingah@gmail.com';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'ADMIN'
WHERE u.email = 'tonykanyingah@gmail.com'
ON CONFLICT (user_id, role_id) DO NOTHING;

