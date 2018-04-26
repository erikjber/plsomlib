# plsomlib
Open source implementation of the PLSOM and PLSOM2 algorithms. Implementations in Java, C#, and C++.

## Project organisation.
The code is split into three subdirectories:  
cpp/  
csharp/   
java/  

Each contains a separate language implementation.

The Java implementation is the most complete one. It also contains a lot of vaguely related code that is only of interest to Machine Learning researchers.

The C# implementation contains a basic implementation of the PLSOM2 algorithm, nothing else. There is no code for saving and loading a map.

The C++ implementation is in progress.

## Usage
Regardless of language, the use of a PLSOM2 is similar. 

### Java example:
```java
// Create an object.
// This example has 60-dimensional input and three output dimensions in a 20x20x20 cube.
PLSOM2 net = new PLSOM2(60,20,20,20);
// set the generalisation factor
net.setNeighbourhoodRange (30);
// Train the network.
for(double [] data:trainingData) 
{
    net.train(data);
}
// Use the network to classify input.
int [] classification  = net.classify(input);
```
### C++ example:
```C++
// Create an object.
// This example has 60-dimensional input and three output dimensions in a 20x20x20 cube.
PLSOM2 map(60,{20,20,20});
// Set the generalisation factor.
map.setNeighbourhoodRange(30);
// Train the network.
for(int x = 0;x<data.size();x++)
{
  map.train(data[x]);
}
// Use the network to classify input.
vector<int> classification = map.classify(input);
```
