CREATE INDEX idx_trend_signals_user_published
    ON trend_signals (user_id, published_at DESC NULLS LAST, id DESC)
    WHERE status = 'ACTIVE';
