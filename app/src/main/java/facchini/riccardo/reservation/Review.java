package facchini.riccardo.reservation;

public class Review
{
    private int reviewScore;
    private String shopUid;
    private String userUid;
    
    public Review(int reviewScore, String shopUid, String userUid)
    {
        this.reviewScore = reviewScore;
        this.shopUid = shopUid;
        this.userUid = userUid;
    }
    
    public int getReviewScore() {return reviewScore;}
    
    public String getShopUid() {return shopUid;}
    
    public String getUserUid() {return userUid;}
}
