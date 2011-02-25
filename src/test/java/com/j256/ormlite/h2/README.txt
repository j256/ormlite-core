I've made the decision to write this H2 classes which implement the support interfaces because
I'm sick of doing all of the -CORE testing through mocks.  A lot of this code is copied
[unfortunately] from the JDBC implementation but I see no other way to do it.   This does mean
as the interfaces are extended that we have to keep another implementation up to date but I
think it is worth it.
