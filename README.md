# MeeshQuest
The project includes three self-designed data structures(BST and SGT and KDT) to store city information, including name, coordinates, radius and color. 
BST is a standard (unbalanced) binary search tree. 
SGT is a combination of extended binary tree and scapegoat tree. 

# Functions 

## Create City: This command has the following form:
<createCity name="Annapolis" y="14" x="12" radius="15" color="red"/>
This command creates a city object with the specified name, coordinates, radius, and color.
Given this command, the program will generate an XML element in the output document
that echoes back all the pertinent information. It has the following form:
<success>
  <command name="createCity"/>
  <parameters>
    <name value="Annapolis"/>
    <x value="12"/>
    <y value="14"/>
    <radius value="15"/>
    <color value="red"/>
  </parameters>
  <output/>
</success>

## List Cities: This command has the following form:
<listCities sortBy="name"/>
where the “sortBy” attribute can be either “name” or “coordnate”. This lists the cities
that exist so far. In the former case, cities are listed in ascending alphabetical order by
the city name (e.g., using java.lang.String.compareTo()), and in the latter case they are
sorted lexicographically according to their (x, y)-coordinates.

## Delete City: 
Removes a city with the specified name from both dictionaries. The command has
a single parameter, the name of the city to delete. Example:
<deleteCity name="Annapolis"/>
The command succeeds if the city is in the dictionary, and otherwise it generates the error
“cityDoesNotExist”. If successful, your program will generate an XML element in the
output document of the following form:
<success>
  <command name="deleteCity"/>
  <parameters>
    <name value="London"/>
  </parameters>
  <output>
    <cityDeleted color="yellow" name="London" radius="0" x="150" y="250"/>
  </output>
</success>

## Clear-All: 
Resets both of the dictionaries (having the effect of deleting all the cities). It has no parameters.
<clearAll/>

## Print the Binary Search Tree: 
This prints the current contents of the binary search tree in a
hierarchical (preorder) manner, so that the XML structure matches the tree’s structure (see
the example below). There are no parameters.
<printBinarySearchTree/>

## Print the SG Tree: 
This prints the current contents of the SG tree in a hierarchical (preorder)
manner, so that the XML structure matches the tree’s structure (see the example below).
There are no parameters.
<printSGTree/>

# Sample Input/Output: 
## Here is a sample input:
```
<commands
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:noNamespaceSchemaLocation="part1in.xsd"
 	spatialWidth="512"
 	spatialHeight="512">
 <createCity name="Chicago" x="81" y="47" radius="5"  color="black"/>
 <createCity name="Atlanta" x="84" y="33" radius="5"  color="black"/>
 <createCity name="Baltimore" y="39" x="76" radius="5" color="black"/>
 <createCity name="Los_Angeles" x="118" y="33" radius="5"  color="black"/>
 <createCity name="Miami" x="80" y="25" radius="5"  color="black"/>
 <printBinarySearchTree/>
 <printSGTree/>
</commands> 
```

## The resulting sample output:
```
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<results>
  <success>
    <command name="createCity"/>
    <parameters>
      <name value="Chicago"/>
      <x value="81"/>
      <y value="47"/>
      <radius value="5"/>
      <color value="black"/>
    </parameters>
    <output/>
  </success>
  <success>
    <command name="createCity"/>
    <parameters>
      <name value="Atlanta"/>
      <x value="84"/>
      <y value="33"/>
      <radius value="5"/>
      <color value="black"/>
    </parameters>
    <output/>
  </success>
  <success>
    <command name="createCity"/>
    <parameters>
      <name value="Baltimore"/>
      <x value="76"/>
      <y value="39"/>
      <radius value="5"/>
      <color value="black"/>
    </parameters>
    <output/>
  </success>
  <success>
    <command name="createCity"/>
    <parameters>
      <name value="Los_Angeles"/>
      <x value="118"/>
      <y value="33"/>
      <radius value="5"/>
      <color value="black"/>
    </parameters>
    <output/>
  </success>
  <success>
    <command name="createCity"/>
    <parameters>
      <name value="Miami"/>
      <x value="80"/>
      <y value="25"/>
      <radius value="5"/>
      <color value="black"/>
    </parameters>
    <output/>
  </success>
  <success>
    <command name="printBinarySearchTree"/>
    <parameters/>
    <output>
      <binarysearchtree>
        <node name="Chicago" x="81" y="47">
          <node name="Atlanta" x="84" y="33">
            <node name="Baltimore" x="76" y="39"/>
          </node>
          <node name="Los_Angeles" x="118" y="33">
            <node name="Miami" x="80" y="25"/>
          </node>
        </node>
      </binarysearchtree>
    </output>
  </success>
  <success>
    <command name="printSGTree"/>
    <parameters/>
    <output>
      <SGTree>
        <internal name="Chicago" x="81" y="47">
          <internal name="Baltimore" x="76" y="39">
            <external name="Baltimore" x="76" y="39"/>
            <internal name="Miami" x="80" y="25">
              <external name="Miami" x="80" y="25"/>
              <external name="Chicago" x="81" y="47"/>
            </internal>
          </internal>
          <internal name="Atlanta" x="84" y="33">
            <external name="Atlanta" x="84" y="33"/>
            <external name="Los_Angeles" x="118" y="33"/>
          </internal>
        </internal>
      </SGTree>
    </output>
  </success>
</results>
```
## PrintKdTree: 
This prints the current contents of the SG kd-tree in a hierarchical (preorder) manner, so that the XML structure matches the tree’s structure (see the example below). There
are no parameters.
<printKdTree/>
This command succeeds if the tree has at least one point, and otherwise an error “mapIsEmpty”
is generated. The structure of the output is similar to that of the SG tree in Part 1 in the
sense that there are two types of nodes, internal and external. Internal nodes differ from the
SG tree in that each stores a splitting dimension, which is either 0 (for x or vertical) or 1 (for
y or horizontal). Rather than storing a City, each internal node only stores the coordinates
of the splitting point.

## nearestNeighbor: 
This command takes the (x, y) coordinates of a point and conducts the nearest neighbor query on the point. 
eg: <nearestNeighbor x="200" y="600"/>
