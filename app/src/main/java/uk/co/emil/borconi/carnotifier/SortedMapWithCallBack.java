package uk.co.emil.borconi.carnotifier;

import java.util.SortedMap;

public interface SortedMapWithCallBack<K,V>  extends SortedMap<K,V> {

    void removenadupdate(Object o);

}
