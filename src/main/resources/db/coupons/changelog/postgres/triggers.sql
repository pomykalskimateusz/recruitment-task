create or replace function update_modified_date_column()
    returns trigger as '
    begin
        NEW.modified_date_timestamp = current_timestamp;
        return NEW;
    end;
' LANGUAGE PLPGSQL;

DROP TRIGGER IF EXISTS update_modified_date_column on "coupon";
CREATE TRIGGER update_modified_date_column
    BEFORE UPDATE
    ON "coupon"
    FOR EACH ROW
    EXECUTE PROCEDURE
        update_modified_date_column();
