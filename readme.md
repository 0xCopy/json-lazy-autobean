
forked from niloc132/drunken-wallhack

GWT AutoBean splittable interface  to read JSON from
a ByteBuffer. Parses the structure of the JSON in a single pass, so you can access a few properties and move on.

ByteSplittable:
    Best option for fully populating a json object graph from the stream
  
Shallow:
    Best option for querying an attribute at a time 

Dummy: 
    Demonstrates the java optimization potential of reading a given stream of bytes without processing it.


 * How fast?  https://gist.github.com/jnorthrup/ffeb96d236ed9a157b6dae
 ``` 
ByteBufferSplittable on https://raw.githubusercontent.com/automenta/traytention/master/data/climateviewer.json
3.26189992E8 bytes in 1.0 seconds, 311.07901763916016mb/second
Gson's JsonParser on https://raw.githubusercontent.com/automenta/traytention/master/data/climateviewer.json
0.0 bytes in 1.0 seconds, 0.0mb/second
ShallowSplittable on https://raw.githubusercontent.com/automenta/traytention/master/data/climateviewer.json
6.50958915E8 bytes in 1.0 seconds, 620.8027982711792mb/second
Placebo on https://raw.githubusercontent.com/automenta/traytention/master/data/climateviewer.json
5.75970197E8 bytes in 1.0 seconds, 549.2879838943481mb/second
ByteBufferSplittable on (heap)https://raw.githubusercontent.com/automenta/traytention/master/data/climateviewer.json
2.96675482E8 bytes in 1.0 seconds, 282.9317874908447mb/second
Gson's JsonParser on (heap)https://raw.githubusercontent.com/automenta/traytention/master/data/climateviewer.json
0.0 bytes in 1.0 seconds, 0.0mb/second
ShallowSplittable on (heap)https://raw.githubusercontent.com/automenta/traytention/master/data/climateviewer.json
4.51353377E8 bytes in 1.0 seconds, 430.44412326812744mb/second
Placebo on (heap)https://raw.githubusercontent.com/automenta/traytention/master/data/climateviewer.json
5.12350031E8 bytes in 1.0 seconds, 488.615065574646mb/second
ByteBufferSplittable on target/test-classes/5k
2.44251099E8 bytes in 1.0 seconds, 232.9359998703003mb/second
ByteBufferSplittable on target/test-classes/5k+whitespace
2.45048034E8 bytes in 1.0 seconds, 233.6960163116455mb/second
ByteBufferSplittable on target/test-classes/20k
2.51704572E8 bytes in 1.0 seconds, 240.04418563842773mb/second
ByteBufferSplittable on target/test-classes/20k+whitespace
2.5128755E8 bytes in 1.0 seconds, 239.64648246765137mb/second
Gson's JsonParser on target/test-classes/5k
1.41908022E8 bytes in 1.0 seconds, 135.3340358734131mb/second
Gson's JsonParser on target/test-classes/5k+whitespace
2.08957056E8 bytes in 1.0 seconds, 199.2769775390625mb/second
Gson's JsonParser on target/test-classes/20k
1.39801431E8 bytes in 1.0 seconds, 133.32503414154053mb/second
Gson's JsonParser on target/test-classes/20k+whitespace
2.05834575E8 bytes in 1.0 seconds, 196.299147605896mb/second
ShallowSplittable on target/test-classes/5k
3.34159479E8 bytes in 1.0 seconds, 318.67931270599365mb/second
ShallowSplittable on target/test-classes/5k+whitespace
3.55394655E8 bytes in 1.0 seconds, 338.93075466156006mb/second
ShallowSplittable on target/test-classes/20k
3.23310183E8 bytes in 1.0 seconds, 308.3326177597046mb/second
ShallowSplittable on target/test-classes/20k+whitespace
3.3700875E8 bytes in 1.0 seconds, 321.3965892791748mb/second


```