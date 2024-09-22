# File Finder Java Application

## About
* File Finder java application allows the user to search for file recursively across the given folders and archive file types

## Pre-requisite
* Require java 1.8 or higher

## Support Archive Types
* war
* ear
* jar
* zip
* tar
* rar

## Build

`mvn clean package`

## Execution

`Usage: java -jar ArchiveFinder.jar` for GUI mode
̧`Usage: java -cp target/ArchiveFinder-1.3.jar org.bn.RecursiveArchiveFinder <search_path> <search_string> <archive_extensions>`

## Example
`java -cp target/ArchiveFinder-1.3.jar org.bn.RecursiveArchiveFinder "/Users/bn/Downloads/" "HELP.md" "ear,zip,war"`

## Screen Reference
* ![GUI mode.png](GUI%20Mode.png)
* ![Console Mode.png](console_mode.png)
* 