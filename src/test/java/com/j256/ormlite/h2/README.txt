I wrote these H2 classes which implement the support interfaces because I was sick of doing all
of the -CORE testing through mocks which after a while seems so arbitrary.

A lot of this code is copied [unfortunately] from the JDBC implementation but I see no other way
to do it.   This does mean as the interfaces are extended that we have to keep another
implementation up to date but I think it is worth it.
