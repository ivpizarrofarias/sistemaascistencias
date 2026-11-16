import { Injectable } from '@angular/core';
import Swal, { SweetAlertIcon } from 'sweetalert2';

@Injectable({ providedIn: 'root' })
export class NotifyService {
  private toast(icon: SweetAlertIcon, title: string) {
    return Swal.fire({
      toast: true,
      position: 'top-end',
      icon,
      title,
      showConfirmButton: false,
      timer: icon === 'error' ? 4500 : 3000,
      timerProgressBar: true,
    });
  }

  success(message: string) {
    return this.toast('success', message);
  }

  error(message: string) {
    return this.toast('error', message);
  }

  info(message: string) {
    return this.toast('info', message);
  }

  warning(message: string) {
    return this.toast('warning', message);
  }
}
