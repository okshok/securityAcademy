INSERT INTO users(email, nickname) VALUES
 ('alice@example.com','alice'),
 ('bob@example.com','bob');

INSERT INTO seasons(name, start_at, end_at, is_active) VALUES
 ('Season 1', CURRENT_TIMESTAMP, DATEADD('DAY', 28, CURRENT_TIMESTAMP), TRUE);

-- Candidates for today
INSERT INTO question_candidates(candidate_date, type, ticker, prompt, pros, cons, importance, impact)
VALUES
 (CURRENT_DATE, 'EARNINGS', 'NVDA', 'NVDA EPS to beat consensus (O/X)?',
  '[{"id":"p1","text":"Datacenter demand strong","sourceUrl":"https://news"}]',
  '[{"id":"c1","text":"Inventory concerns","sourceUrl":"https://news"}]',
  'NVIDIA는 AI 반도체 시장의 선도 기업으로, 데이터센터 수요 증가와 AI 기술 발전의 핵심 수혜자입니다. EPS 컨센서스 상회 여부는 AI 시장의 성장성과 회사의 수익성을 가늠하는 중요한 지표가 됩니다.',
  'EPS 컨센서스 상회 시 AI 관련 주식들의 상승을 견인하고, 반도체 업계 전반의 투자 심리를 개선할 수 있습니다. 반대로 미달 시 AI 버블 우려가 확산되어 관련 종목들의 조정을 야기할 수 있습니다.'),
 (CURRENT_DATE, 'INDEX', NULL, 'S&P500 to close higher than yesterday (O/X)?',
  '[{"id":"p2","text":"Risk-on sentiment","sourceUrl":"https://news"}]',
  '[{"id":"c2","text":"Rate worries","sourceUrl":"https://news"}]',
  'S&P500은 미국 주식 시장 전체를 대표하는 지수로, 전 세계 투자자들의 리스크 온/오프 심리를 반영하는 가장 중요한 지표입니다. 일일 상승/하락 여부는 시장의 단기적 방향성을 가늠하는 핵심 지표가 됩니다.',
  'S&P500 상승 시 글로벌 주식 시장의 상승 모멘텀을 견인하고, 위험 자산에 대한 투자 심리를 개선할 수 있습니다. 반대로 하락 시 안전 자산 선호 현상이 확산되어 채권과 금 등의 수요가 증가할 수 있습니다.');

-- One open question today
INSERT INTO questions(season_id, ticker, prompt, pros, cons, importance, impact, closes_at, status)
VALUES
 (1, 'NVDA', 'NVDA EPS to beat consensus (O/X)?',
  '[{"id":"p1","text":"Datacenter demand strong"}]',
  '[{"id":"c1","text":"Inventory concerns"}]',
  'NVIDIA는 AI 반도체 시장의 선도 기업으로, 데이터센터 수요 증가와 AI 기술 발전의 핵심 수혜자입니다. EPS 컨센서스 상회 여부는 AI 시장의 성장성과 회사의 수익성을 가늠하는 중요한 지표가 됩니다.',
  'EPS 컨센서스 상회 시 AI 관련 주식들의 상승을 견인하고, 반도체 업계 전반의 투자 심리를 개선할 수 있습니다. 반대로 미달 시 AI 버블 우려가 확산되어 관련 종목들의 조정을 야기할 수 있습니다.',
  DATEADD('HOUR', 12, CURRENT_TIMESTAMP), 'OPEN');

INSERT INTO news(ticker, headline, published_at, link) VALUES
 ('NVDA','Analyst raises target', CURRENT_TIMESTAMP, 'https://example.com/n1'),
 ('NVDA','Supply chain improving', CURRENT_TIMESTAMP, 'https://example.com/n2');