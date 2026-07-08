package io.github.sifisofakude.net.serverresults;

import java.io.InputStream;

/**
 * Represents a file to be uploaded through {@link ServerResults}.
 *
 * <p>This class encapsulates the information required to stream a file
 * to a remote server without loading the entire file into memory.</p>
 *
 * <p>The upload process uses the supplied {@link InputStream} directly,
 * making it suitable for large files, Android SAF streams, network streams,
 * and other non-file-system data sources.</p>
 *
 * <h3>Resource Management</h3>
 * <p>This class implements {@link AutoCloseable}. Callers should close
 * the instance when it is no longer needed to release the underlying
 * input stream.</p>
 *
 * <pre>{@code
 * try(FileUpload upload =
 *         new FileUpload("image.jpg", size, inputStream)) {
 *
 *     ServerResults result =
 *         ServerResults.getServerResults(
 *             url,
 *             upload,
 *             "POST"
 *         );
 * }
 * }</pre>
 */
public final class FileUpload implements AutoCloseable	{
	private String fileName;
	private InputStream inputStream;
	private long size;

	/**
	 * Creates a file upload descriptor.
	 *
	 * @param fileName name to be reported to the server during upload
	 * @param fileSize size of the file in bytes
	 * @param inputStream stream containing the file data
	 */
	public FileUpload(
		String fileName,
		long fileSize,
		InputStream inputStream
	)	{
		this.fileName = fileName;
		this.size = fileSize;
		this.inputStream = inputStream;
	}

	/**
	 * Returns the file name that will be sent to the server.
	 *
	 * <p>This value becomes the filename component of the multipart
	 * upload request.</p>
	 *
	 * @return file name
	 */
	public String getFileName()	{
		return fileName;
	}

	/**
	 * Returns the size of the file in bytes.
	 *
	 * <p>This value is used when reporting the content length
	 * during upload.</p>
	 *
	 * @return file size in bytes
	 */
	public long getFileSize()	{
		return size;
	}
	
	/**
	 * Returns the input stream containing the file data.
	 *
	 * <p>The returned stream is consumed by the upload operation.
	 * Callers should not assume the stream can be reused after an
	 * upload completes.</p>
	 *
	 * @return source stream containing file data
	 */
	public InputStream getInputStream()	{
		return inputStream;
	}

	/**
	 * Closes the underlying input stream.
	 *
	 * <p>If the stream is already closed or an error occurs while
	 * closing it, the exception is ignored.</p>
	 *
	 * <p>After this method returns successfully,
	 * {@link #getInputStream()} will return {@code null}.</p>
	 */
	@Override
	public void close()	{
		if(inputStream != null)	{
			try {
				inputStream.close();
				inputStream = null;
			}catch(Exception e) {}
		}
	}
}
