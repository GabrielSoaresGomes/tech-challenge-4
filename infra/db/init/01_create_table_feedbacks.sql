CREATE TABLE IF NOT EXISTS feedbacks (
        id SERIAL PRIMARY KEY,
        description TEXT NOT NULL,
        note INT NOT NULL CHECK (note >= 0 AND note <= 10),
        urgency BOOLEAN DEFAULT FALSE,
        send_date TIMESTAMP DEFAULT NOW() NOT NULL
);
