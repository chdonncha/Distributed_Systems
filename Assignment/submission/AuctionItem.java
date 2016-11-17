public class AuctionItem{
    private int _currentBid;    
    private String _itemName;
    private String _highBidderName;
    private int _highBidderID;
    
    public AuctionItem(String itemName){
        this._currentBid = 0;
        this._itemName = itemName;
        this._highBidderName = null;
    }
    
    public int getCurrentBid(){
        return _currentBid;
    }
    
    public String getItemName(){
        return _itemName;
    }
    
    public int getHighBidderID(){
        return _highBidderID;
    }
    
    public String getHighBidderName(){
        return _highBidderName;
    }
    // Tries and places a bid on an item. If successful it will set the current high bidder
    // to the bidder specified as well as the new high bid amount. Finally it will return true
    // if the bid is now the new high bid and false otherwise.
    public Boolean bidOnItem(String username, int userID, int bidAmount){
        if(_currentBid < bidAmount){
            _currentBid = bidAmount;
            _highBidderName = username;
            _highBidderID = userID;
            return true;
        }
        return false;
    }
    
}
