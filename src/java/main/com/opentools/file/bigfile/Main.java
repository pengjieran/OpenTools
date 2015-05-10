package com.opentools.file.bigfile;

public class Main {

	public static void main(String[] args) {

		BigFileReader.Builder builder = new BigFileReader.Builder("e:/reliability.txt", new FileHandle() {

					@Override
					public void handle(String line) {
						System.out.println(line);
						
					}
				});
		builder.withTreahdSize(10).withCharset("utf8")
				.withBufferSize(1024 * 1024);
		BigFileReader bigFileReader = builder.build();
		bigFileReader.start();
	}

}