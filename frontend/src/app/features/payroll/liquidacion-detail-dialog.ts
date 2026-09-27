import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTableModule } from '@angular/material/table';
import { firstValueFrom } from 'rxjs';

import { LiquidacionResponse } from '../../core/models/api.models';
import { ApiService } from '../../core/services/api.service';
import { FileDownloadService } from '../../core/services/file-download.service';
import { apiErrorMessage } from '../../core/services/auth.service';
import { formatMoney, liquidacionFileName, monthLabel } from '../../core/utils/format';

export interface LiquidacionDetailData {
  liquidacion: LiquidacionResponse;
  /** Cuando la liquidación se acaba de generar, se ofrece la descarga del PDF. */
  fromGenerate?: boolean;
}

/** Detalle de una liquidación con sus haberes, descuentos y PDF. */
@Component({
  selector: 'app-liquidacion-detail-dialog',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [MatButtonModule, MatDialogModule, MatIconModule, MatProgressBarModule, MatTableModule],
  templateUrl: './liquidacion-detail-dialog.html',
  styleUrl: './liquidacion-detail-dialog.scss',
})
export class LiquidacionDetailDialog {
  private readonly api = inject(ApiService);
  private readonly downloader = inject(FileDownloadService);
  private readonly ref = inject<MatDialogRef<LiquidacionDetailDialog>>(MatDialogRef);

  readonly data = inject<LiquidacionDetailData>(MAT_DIALOG_DATA);
  readonly liq = this.data.liquidacion;

  readonly downloading = signal(false);
  readonly error = signal('');

  readonly formatMoney = formatMoney;
  readonly periodo = monthLabel(this.liq.periodYear, this.liq.periodMonth);
  readonly columns = ['concepto', 'tipo', 'monto'];

  async descargarPdf(): Promise<void> {
    this.downloading.set(true);
    this.error.set('');
    try {
      const response = await firstValueFrom(this.api.downloadLiquidacionPdf(this.liq.id));
      this.downloader.saveResponse(
        response,
        liquidacionFileName(this.liq.employeeName, this.liq.periodYear, this.liq.periodMonth),
      );
    } catch (err: unknown) {
      this.error.set(apiErrorMessage(err, 'No se pudo descargar el PDF'));
    } finally {
      this.downloading.set(false);
    }
  }

  close(): void {
    this.ref.close();
  }
}
