-- =========================================
-- Migration SQL pour StudyTimer (PostgreSQL)
-- =========================================

-- 1. Extension UUID (si nécessaire)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 2. TYPES ENUM
CREATE TYPE user_role AS ENUM ('owner', 'member');
CREATE TYPE session_state AS ENUM ('Idle', 'Focus', 'Break');
CREATE TYPE friendship_status AS ENUM ('pending', 'accepted');

-- 3. TABLES PRINCIPALES

-- Utilisateurs
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email TEXT       NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    display_name TEXT,
    avatar_url TEXT,
    verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Rooms de révision
CREATE TABLE rooms (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    owner_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    subject TEXT   NOT NULL,
    level TEXT     NOT NULL,
    topic TEXT,
    institution TEXT,
    focus_duration INTEGER NOT NULL DEFAULT 25,
    break_duration INTEGER NOT NULL DEFAULT 5,
    theme_config JSONB,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Participants de room
CREATE TABLE room_members (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    room_id UUID NOT NULL REFERENCES rooms(id) ON DELETE CASCADE,
    joined_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    role user_role NOT NULL DEFAULT 'member',
    PRIMARY KEY (user_id, room_id)
);

-- Sessions Pomodoro
CREATE TABLE sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    room_id UUID NOT NULL REFERENCES rooms(id) ON DELETE CASCADE,
    start_time TIMESTAMPTZ NOT NULL,
    end_time   TIMESTAMPTZ,
    state session_state NOT NULL DEFAULT 'Idle'
);

-- Messages de chat dans une room
CREATE TABLE room_messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    room_id UUID NOT NULL REFERENCES rooms(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE SET NULL,
    message TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Prompts et réponses du chatbot IA
CREATE TABLE prompts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    room_id UUID NOT NULL REFERENCES rooms(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE SET NULL,
    prompt_text TEXT NOT NULL,
    ai_response TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Messages privés (DM)
CREATE TABLE dm_messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    sender_id   UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    receiver_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    message TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    -- Pour retrouver la conversation A↔B plus facilement
    CHECK (sender_id <> receiver_id)
);

-- Amitiés et invitations
CREATE TABLE friendships (
    requester_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    target_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status friendship_status NOT NULL DEFAULT 'pending',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (requester_id, target_id),
    CHECK (requester_id <> target_id)
);

-- Statistiques hebdomadaires de focus (optionnel)
CREATE TABLE focus_stats (
    user_id UUID     NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    week_start DATE  NOT NULL,           -- lundi de la semaine
    focus_minutes INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (user_id, week_start)
);

-- 4. INDEX & OPTIMISATIONS

-- Accélérer recherche messages récents en room
CREATE INDEX idx_room_messages_room_created
  ON room_messages (room_id, created_at DESC);

-- Accélérer stats hebdo
CREATE INDEX idx_focus_stats_week
  ON focus_stats (user_id, week_start);

-- 5. VUES ou MATÉRIALISÉES (exemple)
-- Exemple de vue matérialisée pour stats cumulées
CREATE MATERIALIZED VIEW mv_user_weekly_focus AS
SELECT
  user_id,
  week_start,
  focus_minutes
FROM focus_stats
WITH NO DATA;

-- Rafraîchir périodiquement :
-- REFRESH MATERIALIZED VIEW mv_user_weekly_focus;

-- =========================================
-- Fin du script de migration initiale
-- =========================================
