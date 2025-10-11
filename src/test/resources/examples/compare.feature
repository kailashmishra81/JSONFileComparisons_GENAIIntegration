Feature: Compare multiple JSONs using Java

  Scenario: Compare multiple JSON files
    # Load Java classes
    * def JsonComparator = Java.type('com.example.jsoncompare.JsonComparator')
    * def JsonFileHelper = Java.type('com.example.jsoncompare.JsonFileHelper')
    * def OpenAIHelper = Java.type('com.example.jsoncompare.OpenAIHelper')

    # Folder containing JSONs grouped by scenario
    * def folderPath = 'target/target/json-output/Feature1'
    * def files = JsonFileHelper.getJsonFilesGroupedByScenario(folderPath)

    # Compare scenario-wise
   # * def differences = JsonComparator.compareAllScenarios(files)
  #  * print differences

    # Summarize additions/removals scenario-wise
  #  * def summary = JsonComparator.summarizeAdditionsRemovalsByScenario(files)
   # * print summary


  # Print scenario-wise summary with real newlines
 # convert Java Map keys to array


# iterate over each scenario
 # summary is a Java Map<String, String> but seen as JS object
     # Print scenario-wise summary with proper newlines
#    * eval
#    """
#  for (var scenario in summary) {
#  karate.log('==== ' + scenario + ' ====');
#  var text = summary[scenario] ? summary[scenario].toString() : '';
#  text.split(/\\r?\\n/).forEach(function(line) {
#  if (line && line.trim() !== '') {
#  karate.log(line);
#  }
#  });
#  }
#  """






    # Optionally, call OpenAI or analyze further

  # Convert inner arrays to Java List
#* eval
#"""
#for (var key in summary) {
#  summary[key] = Java.to(summary[key], 'java.util.List');
#}
#"""

    * def apiKey = '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'

# Call the Java helper
    * def OpenAIHelper = Java.type('com.example.jsoncompare.OpenAIFilesComparison')
    * def result = OpenAIHelper.getAiSummary(summary, apiKey)

# Print the result
    * print result