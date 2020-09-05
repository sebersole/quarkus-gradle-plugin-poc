package org.hibernate.build.gradle.quarkus;

/**
 * @author Steve Ebersole
 */
public class NativeArguments {
	private final QuarkusDsl quarkusDsl;

	private boolean containerBuild;
	private String buildImage;

	public NativeArguments(QuarkusDsl quarkusDsl) {
		this.quarkusDsl = quarkusDsl;
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
