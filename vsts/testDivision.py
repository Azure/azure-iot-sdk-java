#need to "mvn clean test-compile" first to have maven generate these input files!!!!

import os
import sys

outputFileBaseName = 'testsToRun'

#Get input args for directory to scan, and number of output files to split the tests up into
numberOfAgents = 1
if len(sys.argv) == 0:
	print('Usage: python testDivision.py <rootDir> <number of build agents to create files for>')
	sys.exit('Invalid input parameters')
	
rootDir = sys.argv[1]

if len(sys.argv) < 3:
	print('No input argument for build agent number provided, assuming only one agent will be used')
else:
	print('Outputting files for ' + sys.argv[2] + ' build agents')
	numberOfAgents = int(sys.argv[2])
	
	
# Search recursively through the provided root directory for "inputFile.lst" and save each one's contents.
# These files contain a list of all test suites by their full path name.
fileNames = []
unitTests = []
integrationTests = []
errorInjectionTests = []
for dirName, subdirList, fileList in os.walk(rootDir):
	for fname in fileList:
		#"inputFiles" is used in test compilation and normal compilation, but this script only cares about test compilation
		if fname == "inputFiles.lst" and 'default-testCompile' in dirName:
			with open(dirName + "/" + fname) as f:
				content = f.readlines()
				
				#remove whitespace characters like `\n` at the end of each line
				content = [x.strip() for x in content] 
				
				for i in range(len(content)):
					#parse just the <filename>.java and save for later
					file = open(content[i])
					fileName = os.path.basename(file.name)
					if 'ErrInj' in fileName:
						errorInjectionTests.append(fileName)
					elif 'JVMRunner' in fileName:
						integrationTests.append(fileName)
					else:
						unitTests.append(fileName)
				

# The only sorting philosophy here is to make sure that all error injection tests are next to each other and all integration tests
# are next to each other, too. That way the division below will never put more than 1 extra errorInjection test suite
# in a set of tests compared to the other sets of tests. This is done because the error injection tests take the longest amount of time
# and the regular integration tests also run for longer than the unit tests.
# In order for this sorting philosophy to work well, no test suite should run for significantly longer than any other
# comparable test suite.
fileNames.extend(unitTests)
fileNames.extend(integrationTests)
fileNames.extend(errorInjectionTests)


#Divide up tests such that each agent takes turns plucking the topmost test from the total list.
# No set of test suites will have more than 1 extra test suite than any other set
firstEntry = True
for agentIndex in range(numberOfAgents):
	with open(outputFileBaseName + str(agentIndex) + '.txt', 'w') as f:
		subList = fileNames[agentIndex::numberOfAgents]
		for j in range(len(subList)):
			if not firstEntry:
				f.write(',')
				
			f.write(subList[j])
			firstEntry = False
				

				