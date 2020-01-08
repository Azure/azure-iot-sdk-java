﻿./vsts/determine_tests_to_run.ps1

mvn -DRUN_PROVISIONING_TESTS="$Env:runProvisioningTests" -DRUN_IOTHUB_TESTS="$Env:runIotHubTests" install -DskipITs=false -T 2C "-Dfailsafe.rerunFailingTestsCount=2" #parallelized to use 2 threads per core