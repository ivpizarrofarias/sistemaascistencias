import { HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';

/**
 * Descarga un `Blob` como archivo local.
 *
 * Se usa para los PDF de liquidación y de horario, que el backend genera.
 */
@Injectable({ providedIn: 'root' })
export class FileDownloadService {
  save(blob: Blob, filename: string): void {
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  }

  /**
   * Guarda la respuesta de una descarga usando el nombre que Sendingó el
   * backend en `Content-Disposition`, para que el archivo se llame igual en
   * toda la app. Si el header no viene, se usa `fallback`.
   */
  saveResponse(response: HttpResponse<Blob>, fallback: string): void {
    if (!response.body) return;
    this.save(response.body, this.fileNameFrom(response, fallback));
  }

  private fileNameFrom(response: HttpResponse<Blob>, fallback: string): string {
    const header = response.headers.get('Content-Disposition') ?? '';
    // El backend envía la versión con tildes en UTF-8 (RFC 5987) y una ASCII
    // de respaldo, así que se prefiere la primera.
    const utf8 = /filename\*=UTF-8''([^;]+)/i.exec(header);
    if (utf8) {
      try {
        return decodeURIComponent(utf8[1]);
      } catch {
        /* nombre mal formado: se cae al respaldo */
      }
    }
    const plain = /filename="?([^";]+)"?/i.exec(header);
    return plain ? plain[1] : fallback;
  }
}
