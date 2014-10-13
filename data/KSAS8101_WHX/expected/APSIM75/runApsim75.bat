@echo off
if exist "C:\Program Files (x86)\Apsim75-r3008\Model\" (
  if exist wheat.xml (
    echo Run APSIM...
    "C:\Program Files (x86)\Apsim75-r3008\Model\Apsim" AgMip.apsim
    echo done!
  ) else (
    echo Copy model file into working directory...
    if exist "C:\Program Files (x86)\Apsim75-r3008\Model\wheat.xml" (
      copy "C:\Program Files (x86)\Apsim75-r3008\Model\wheat.xml" wheat.xml
      echo done!
      echo Run APSIM...
      "C:\Program Files (x86)\Apsim75-r3008\Model\Apsim" AgMip.apsim
      echo done!
    ) else (
      echo Failed! Crop file is missing!
      pause
    )
  )
) else if exist "C:\Program Files\Apsim75-r3008\Model\" (
  if exist wheat.xml (
    echo Run APSIM...
    "C:\Program Files\Apsim75-r3008\Model\Apsim" AgMip.apsim
    echo done!
  ) else (
    echo Copy model file into working directory...
    if exist "C:\Program Files\Apsim75-r3008\Model\wheat.xml" (
      copy "C:\Program Files\Apsim75-r3008\Model\wheat.xml" wheat.xml
      echo done!
      echo Run APSIM...
      "C:\Program Files\Apsim75-r3008\Model\Apsim" AgMip.apsim
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
