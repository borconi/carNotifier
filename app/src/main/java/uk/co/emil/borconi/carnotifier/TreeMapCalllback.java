package uk.co.emil.borconi.carnotifier;


import java.util.TreeMap;

public class TreeMapCalllback<K,V> extends TreeMap<K,V> implements SortedMapWithCallBack<K,V> {

    PlayBackQue playBackQue;
    public TreeMapCalllback(PlayBackQue playBackQue) {
       this.playBackQue=playBackQue;
    }

    @Override
    public void removenadupdate(Object o) {
        super.remove(o);
        playBackQue.isPlaying=false;
        playBackQue.playQuedMessages();
    }



}
