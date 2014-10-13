@echo off
if exist "C:\Program Files (x86)\Apsim74-r2286\Model\" (
  if exist peanut.xml (
    echo Run APSIM...
    "C:\Program Files (x86)\Apsim74-r2286\Model\ApsimRun" AgMip.apsim
    echo done!
  ) else (
    echo Copy model file into working directory...
    if exist "C:\Program Files (x86)\Apsim74-r2286\Model\peanut.xml" (
      copy "C:\Program Files (x86)\Apsim74-r2286\Model\peanut.xml" peanut.xml
      echo done!
      echo Run APSIM...
      "C:\Program Files (x86)\Apsim74-r2286\Model\ApsimRun" AgMip.apsim
      echo done!
    ) else (
      echo Failed! Crop file is missing!
      pause
    )
  )
) else if exist "C:\Program Files\Apsim74-r2286\Model\" (
  if exist peanut.xml (
    echo Run APSIM...
    "C:\Program Files\Apsim74-r2286\Model\ApsimRun" AgMip.apsim
    echo done!
  ) else (
    echo Copy model file into working directory...
    if exist "C:\Program Files\Apsim74-r2286\Model\peanut.xml" (
      copy "C:\Program Files\Apsim74-r2286\Model\peanut.xml" peanut.xml
      echo done!
      echo Run APSIM...
      "C:\Program Files\Apsim74-r2286\Model\ApsimRun" AgMip.apsim
      echo done!
    ) else (
      echo Failed! Crop file is missing!
      pause
    )
  )
) else  (
  echo APSIM Model is missing!
  pause
)
