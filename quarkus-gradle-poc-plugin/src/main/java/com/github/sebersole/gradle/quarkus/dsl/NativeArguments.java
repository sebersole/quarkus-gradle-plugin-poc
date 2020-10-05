package com.github.sebersole.gradle.quarkus.dsl;

/**
 * ???
 */
public class NativeArguments {
	private boolean containerBuild;
	private String buildImage;

	public NativeArguments() {
	}

	public boolean isContainerBuild() {
		return containerBuild;
	}

	public void setContainerBuild(boolean containerBuild) {
		this.containerBuild = containerBuild;
	}

	public void containerBuild(boolean containerBuild) {
		setContainerBuild( containerBuild );
	}

	public String getBuildImage() {
		return buildImage;
	}

	public void setBuildImage(String buildImage) {
		this.buildImage = buildImage;
	}

	public void buildImage(String buildImage) {
		setBuildImage( buildImage );
	}
}
