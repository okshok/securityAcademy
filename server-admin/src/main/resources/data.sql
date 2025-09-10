INSERT INTO users(email, nickname) VALUES
 ('alice@example.com','alice'),
 ('bob@example.com','bob');

INSERT INTO seasons(name, start_at, end_at, is_active) VALUES
 ('Season 1', CURRENT_TIMESTAMP, DATEADD('DAY', 28, CURRENT_TIMESTAMP), TRUE);

-- Candidates for today
INSERT INTO question_candidates(candidate_date, type, ticker, prompt, pros, cons)
VALUES
 (CURRENT_DATE, 'EARNINGS', 'NVDA', 'NVDA EPS to beat consensus (O/X)?',
  '[{"id":"p1","text":"Datacenter demand strong","sourceUrl":"https://news"}]',
  '[{"id":"c1","text":"Inventory concerns","sourceUrl":"https://news"}]'),
 (CURRENT_DATE, 'INDEX', NULL, 'S&P500 to close higher than yesterday (O/X)?',
  '[{"id":"p2","text":"Risk-on sentiment","sourceUrl":"https://news"}]',
  '[{"id":"c2","text":"Rate worries","sourceUrl":"https://news"}]');

-- One open question today
INSERT INTO questions(season_id, ticker, prompt, pros, cons, closes_at, status)
VALUES
 (1, 'NVDA', 'NVDA EPS to beat consensus (O/X)?',
  '[{"id":"p1","text":"Datacenter demand strong"}]',
  '[{"id":"c1","text":"Inventory concerns"}]',
  DATEADD('HOUR', 12, CURRENT_TIMESTAMP), 'OPEN');

INSERT INTO news(ticker, headline, published_at, link) VALUES
 ('NVDA','Analyst raises target', CURRENT_TIMESTAMP, 'https://example.com/n1'),
 ('NVDA','Supply chain improving', CURRENT_TIMESTAMP, 'https://example.com/n2');