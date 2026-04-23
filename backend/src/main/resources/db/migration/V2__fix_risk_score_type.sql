-- Fix risk_score columns from SMALLINT to INTEGER to match Hibernate entity mapping
ALTER TABLE endpoints ALTER COLUMN risk_score TYPE INTEGER USING risk_score::INTEGER;
ALTER TABLE threats   ALTER COLUMN risk_score TYPE INTEGER USING risk_score::INTEGER;
