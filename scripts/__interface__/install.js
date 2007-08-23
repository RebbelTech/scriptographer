/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2007 Juerg Lehni, http://www.scratchdisk.com.
 * All rights reserved.
 *
 * Please visit http://scriptographer.com/ for updates and contact.
 *
 * -- GPL LICENSE NOTICE --
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * -- GPL LICENSE NOTICE --
 *
 * File created on 22.08.2007.
 *
 * $Id$
 */

if (app.macintosh) {
	function executeProcess(command, input) {
		var process = java.lang.Runtime.getRuntime().exec(command);
		if (input) {
			var out = new java.io.PrintStream(process.getOutputStream());
			out.print(input);
			out.flush();
		}
		// Wait for the process to finish.
		// Unfortunatelly process.waitFor does not know waitFor(timeout)
		var time = new Date().getTime(), exc;
		while(new Date().getTime() - time < 500) {
			try {
				process.exitValue();
				break;
			} catch (e) {
			}
			java.lang.Thread.sleep(10);
		}
		process.exitValue();
		function readStream(stream) {
			var reader = new java.io.BufferedReader(new java.io.InputStreamReader(stream));
			var res = [], line, first = true;
			while ((line = reader.readLine()) != null) {
				if (first) first = false;
				else res.push(lineBreak);
				res.push(line);
			}
			return res.join('');
		}

		var error = readStream(process.getErrorStream());
		if (error && !(/^\s*WARNING:/.test(error)))
			throw 'Error in command \'' + command + '\': ' + error;

		var ret = readStream(process.getInputStream());
		process.destroy();
		return ret;
	}

	// Determine current user and see if it is part of the uucp group, as required by RXTX
	var user = executeProcess('id -p').match(/uid.(\w*)/)[1];
	var groups = executeProcess('niutil -readprop / /groups/uucp users').split(/\n/);
	var found = false;
	for (var i = 0; i < groups.length && !found; i++)
		found = groups[i] == user;
	// Also create /var/Lock if it does not exist yet.
	var file = new java.io.File('/var/lock');
	if (!file.exists() || !found) {
		var dialog = new ModalDialog(function() {
			this.title = "Scriptographer Setup";

			var logo = new ImageStatic(this);
			logo.image = getImage("logo.png");

			var text = new Static(this) {
				text: 'You appear to be runing Scriptographer for the first time.\n\n' +
					'If you would like to use the included RXTX library for\n' +
					'serial port communication, some modifications would need\n' +
					'to be made now.\n\n' +
					'Please enter your password now:'
			};

			this.passwordField = new TextEdit(this, TextEdit.OPTION_PASSWORD);

			this.cancelItem = new Button(this) {
				text: 'Cancel'
			};
			this.defaultItem = new Button(this) {
				text: '  OK  '
			};
			this.margins = 10;
			this.layout = new TableLayout([
					[ 'prefered', 'fill', 'prefered', 'prefered' ],
					[ 'prefered', 'fill', 'prefered', 'prefered' ]
				], 4, 4);
			this.content = {
				'0, 0': logo,
				'1, 0, 3, 1': text,
				'1, 2, 3, 2': this.passwordField,
				'2, 3': this.cancelItem,
				'3, 3': this.defaultItem
			}
		});
		var tryAgain = true;
		while (tryAgain && dialog.doModal() == dialog.defaultItem) {
			var password = dialog.passwordField.text + '\n';
			try {
				executeProcess('sudo -K');
				executeProcess('sudo -v', password);
				if (!file.exists()) {
					executeProcess('sudo mkdir /var/lock');
					executeProcess('sudo chgrp uucp /var/lock');
					executeProcess('sudo chmod 775 /var/lock');
				}
				if (!found) {
					executeProcess('sudo niutil -mergeprop / /groups/uucp users ' + user);
				}
				executeProcess('sudo -K');
		  		Dialog.alert("Finished making changes, you should be all set now.\n\nHave fun!");
				tryAgain = false;
			} catch (e) {
				tryAgain = Dialog.confirm('You do not seem to have the required permissions.\n' +
					'Would you like to reenter your password now?');
			}
		}
	}
}
