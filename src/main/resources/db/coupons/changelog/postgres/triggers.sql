create or replace function update_modified_date_column()
    returns trigger as '
    begin
        NEW.modified_date_timestamp = current_timestamp;
        return NEW;
    end;
' LANGUAGE PLPGSQL;

create or replace function normalize_coupon_code()
    returns trigger AS '
    begin
        NEW.code_normalized := UPPER(NEW.code);
        RETURN NEW;
    end;
' LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS update_modified_date_column on "coupon";
CREATE TRIGGER update_modified_date_column
    BEFORE UPDATE
    ON "coupon"
    FOR EACH ROW
    EXECUTE PROCEDURE
        update_modified_date_column();

DROP TRIGGER IF EXISTS normalize_coupon_code ON coupon;
CREATE TRIGGER normalize_coupon_code
    BEFORE INSERT OR UPDATE ON coupon
                         FOR EACH ROW
                         EXECUTE FUNCTION normalize_coupon_code();