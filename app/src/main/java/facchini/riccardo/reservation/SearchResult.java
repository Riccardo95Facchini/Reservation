package facchini.riccardo.reservation;

import facchini.riccardo.reservation.Shop_Package.Shop;

public class SearchResult
{
    private Shop shopFound;
    private float distance;
    
    public SearchResult(Shop shopFound, float distance)
    {
        this.shopFound = shopFound;
        this.distance = distance;
    }
    
    public Shop getShopFound() {return shopFound;}
    
    public float getDistance() {return distance;}
    
    public String getFormatDistance()
    {
        return distance > 1000f ? String.format("Distance: %.2f Km", distance / 1000) : String.format("Distance %.2f m", distance);
    }
    
    public int compareTo(SearchResult sr2)
    {
        if (distance > sr2.distance)
            return 1;
        else if (distance < sr2.distance)
            return -1;
        else return 0;
    }
}
