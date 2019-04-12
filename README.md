# Big-Blast

Tool to perform Blast using Blast+. This tool should be used when the blast output is expected to be large and only the results like query_coverage, e-value, bit score (and a few others) are important. The output is a JSON file containing this data.

This was developed using a pre installed version of NCBI Blast+. A dokerfile is provided if you want to run it inside a container.

File Blast-0.0.1-SNAPSHOT.zip contains a runnable .jar file with the last stable version of the software.