package com.aplana.sbrf.taxaccounting.web.widget.pdfviewer.server;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Вспомогательная утилита для реализации серверной части компонента PDFViewer
 * 
 * @author sgoryachkin
 * 
 */
public final class PDFImageUtils {

	private PDFImageUtils() {
	}

	/**
	 * Пишем в image изображение страницы документа
	 * 
	 * @param pdf
	 * @param image
	 * @param pageIndex
	 * @param imageFormat
	 * @param imageResolution
	 */
	public static void pDFPageToImage(InputStream pdf, OutputStream image,
			int pageIndex, String imageFormat, int imageResolution) {

		PDDocument pdDocument = null;
		try {
			pdDocument = PDDocument.load(pdf);

			PDPageTree pages = pdDocument.getDocumentCatalog().getPages();

			PDFRenderer pdfRenderer = new PDFRenderer(pdDocument);

			BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(
					pageIndex, imageResolution, ImageType.GRAY);
			if (!ImageIOUtil.writeImage(bufferedImage, imageFormat, image)) {
				throw new RuntimeException(
						"Не удалось сформировать изображение для страницы "
								+ pageIndex);
			}
		} catch (Exception e) {
			throw new RuntimeException(
					"Не удалось сформировать изображение для страницы "
							+ pageIndex, e);
		} finally {
			if (pdDocument != null) {
				try {
					pdDocument.close();
				} catch (IOException e) {
					//
				}
			}
		}
	}

	/**
	 * Получаем количество страниц PDF документа
	 * 
	 * @param pdf
	 * @return
	 */
	public static int getPageNumber(InputStream pdf) {
		PDDocument pdDocument = null;
		try {
			pdDocument = PDDocument.load(pdf);
			return pdDocument.getDocumentCatalog().getPages().getCount();
		} catch (Exception e) {
			throw new RuntimeException("Не удалось получить количество страниц",
					e);
		} finally {
			if (pdDocument != null) {
				try {
					pdDocument.close();
				} catch (IOException e) {
					//
				}
			}
		}
	}
}
