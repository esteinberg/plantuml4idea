package org.plantuml.idea;

public class InstallationError extends RuntimeException {
	public InstallationError(String s, NoClassDefFoundError e) {
		super(s, e);
	}
}
