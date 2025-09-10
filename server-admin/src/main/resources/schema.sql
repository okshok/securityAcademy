DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS seasons;
DROP TABLE IF EXISTS questions;
DROP TABLE IF EXISTS question_candidates;
DROP TABLE IF EXISTS predictions;
DROP TABLE IF EXISTS resolutions;
DROP TABLE IF EXISTS scores;
DROP TABLE IF EXISTS news;

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    nickname VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE seasons (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    start_at TIMESTAMP NOT NULL,
    end_at TIMESTAMP NOT NULL,
    is_active BOOLEAN DEFAULT FALSE
);

CREATE TABLE question_candidates (
    id BIGSERIAL PRIMARY KEY,
    candidate_date DATE NOT NULL,
    type VARCHAR(50) NOT NULL,
    ticker VARCHAR(20),
    prompt VARCHAR(1000) NOT NULL,
    pros CLOB,
    cons CLOB,
    importance VARCHAR(2000),
    impact VARCHAR(2000),
    status VARCHAR(50) DEFAULT 'CANDIDATE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE questions (
    id BIGSERIAL PRIMARY KEY,
    season_id BIGINT NOT NULL,
    ticker VARCHAR(20),
    prompt VARCHAR(1000) NOT NULL,
    pros CLOB,
    cons CLOB,
    importance VARCHAR(2000),
    impact VARCHAR(2000),
    closes_at TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL
);

CREATE TABLE predictions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    choice VARCHAR(5) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE resolutions (
    question_id BIGINT PRIMARY KEY,
    outcome VARCHAR(10) NOT NULL,
    resolved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    proof_url VARCHAR(500)
);

CREATE TABLE scores (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    season_id BIGINT NOT NULL,
    total_points INT DEFAULT 0
);

CREATE TABLE news (
    id BIGSERIAL PRIMARY KEY,
    ticker VARCHAR(20) NOT NULL,
    headline VARCHAR(500) NOT NULL,
    published_at TIMESTAMP NOT NULL,
    link VARCHAR(500)
);

CREATE INDEX idx_questions_season ON questions(season_id);
CREATE UNIQUE INDEX uq_prediction_user_question ON predictions(user_id, question_id);