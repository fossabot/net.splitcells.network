#!/usr/bin/env python3
"""
This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0, which is available at
http://www.eclipse.org/legal/epl-2.0, or the MIT License,
which is available at https://spdx.org/licenses/MIT.html.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License, version 2
or any later versions with the GNU Classpath Exception which is
available at https://www.gnu.org/software/classpath/license.html.
"""

__author__ = "Mārtiņš Avots"
__authors__ = ["and other"]
__copyright__ = "Copyright 2021"
__license__ = "EPL-2.0 OR MIT OR GPL-2.0-or-later WITH Classpath-exception-2.0"
# TODO Create tests.
# TODO PERFORMANCE compile to C via https://cython.org/.
import argparse
import os
from pathlib import Path
from pathlib import PosixPath
import shutil
import re
from os import environ
import logging
def currentFolder():
	return Path.cwd()
class Command:
	def __init__(self
			, commandPath
			, targetFolder = Path.home().joinpath('bin', 'net.splitcells.os.state.interface.commands.managed')
			):
		commandPosixPath = PosixPath(commandPath)
		self.name = commandPosixPath.name
		self.sourceFolder = commandPosixPath.parent
		self.targetFolder = targetFolder
		self.managedCommandNaming = "commandName = '" + self.name + "'"
	def install(self):
		executionCounter = 0
		targetFileName = self.name
		if (self.name.endswith('.sh')):
			targetFileName = self.name[:-3]
		if (self.name.endswith('.py')):
			targetFileName = self.name[:-3]
		targetFile = self.targetFolder.joinpath(targetFileName) # TODO Create test, where a command is installed and check if suffix processing works correctly.
		if targetFile.exists():
			while True:
				targetFile = self.targetFolder.joinpath(self.name + '.' + str(executionCounter))
				executionCounter+=1
				if not targetFile.exists():
					break
		shutil.copy(self.sourceFolder.joinpath(self.name), targetFile)
		if 'current_echo_level' in os.environ:
			if int(os.environ['current_echo_level']) >= 5:
				print(self.name + ' installed.')
if __name__ == '__main__':
	if environ.get('log_level') == 'debug':
		logging.basicConfig(format='%(message)s', level=logging.DEBUG)
	else:
	    logging.basicConfig(format='%(message)s', level=logging.INFO)
	parser = argparse.ArgumentParser(description='Installs command to ~/bin and integrates commands already present.') # TODO If command already present, check if it is a manager. If this is not the case, throw an error.
	parser.add_argument('commandToInstall', nargs='?', type=str, help='The name of the command that is installed.')
	parsedArgs = parser.parse_args()
	Command(parsedArgs.commandToInstall).install()
