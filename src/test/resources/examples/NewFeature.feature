Feature: Multiple scenarios  multiple JSONs using Java
  Background:
    # Include hooks feature

  * def  scenarioName = karate.info.scenarioName;  // current scenario
    * def JsonComparator = Java.type('com.example.jsoncompare.JsonComparator')
    * def JsonFileHelper = Java.type('com.example.jsoncompare.JsonFileHelper')
    * def LocalDateTime = Java.type('java.time.LocalDateTime')
    * def DateTimeFormatter = Java.type('java.time.format.DateTimeFormatter')
    * def formatter = DateTimeFormatter.ofPattern('yyyy-MM-dd_HH-mm-ss')
    * def timestamp = LocalDateTime.now().format(formatter)



  Scenario: API Call 1
Given url 'https://jsonplaceholder.typicode.com/users/1'
When method get
Then status 200
* def fileName = 'target/json-output/Feature1/' + scenarioName + '/response_' + scenarioName + "_" + timestamp + '.json'
    * eval karate.write(karate.pretty(response), fileName)
#    * def folderPath = 'target/target/json-output/Feature1/API Call 1'
#    * def files = JsonFileHelper.getJsonFilesFromFolder(folderPath)
#
#    * def files = JsonFileHelper.getAllJsonFilesRecursively(folderPath)
#
#
#    * def summary = JsonComparator.summarizeAdditionsRemovals(files)
#    * print summary


Scenario: API Call 2
Given url 'https://jsonplaceholder.typicode.com/users/2'
When method get
Then status 200
  * def fileName = 'target/json-output/Feature1/' + scenarioName + '/response_' + scenarioName + "_"+ timestamp + '.json'
  * eval karate.write(karate.pretty(response), fileName)
#  * def folderPath = 'target/target/json-output/Feature1/API Call 2'
#
#  * def files = JsonFileHelper.getAllJsonFilesRecursively(folderPath)
#
#  * def summary = JsonComparator.summarizeAdditionsRemovals(files)
#  * print summary
#


  Scenario: API Call 3
    Given url 'https://jsonplaceholder.typicode.com/users/3'
    When method get
    Then status 200
    * def fileName = 'target/json-output/Feature1/' + scenarioName + '/response_' + scenarioName + "_"+ timestamp + '.json'
    * eval karate.write(karate.pretty(response), fileName)
#    * def folderPath = 'target/target/json-output/Feature1/API Call 3'
#    * def files = JsonFileHelper.getJsonFilesFromFolder(folderPath)
#    * def files = JsonFileHelper.getAllJsonFilesRecursively(folderPath)
#
#    * def summary = JsonComparator.summarizeAdditionsRemovals(files)
#    * print summary


