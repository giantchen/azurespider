package phenom.stock;

/**
 * TODO: type comment.
 * 
 */
public enum Cycle {
    FIVE_DAYS(5), TEN_DAYS(10), FIFTEEN_DAYS(15), THIRTY_DAYS(30), ONE_HUNDRED_TWENTY_DAYS(120), TWO_HUNDRED_DAYS(200);

    private int day;

    private Cycle(int d_) {
        day = d_;
    }

    public int numDays() {
        return day;
    }
}
