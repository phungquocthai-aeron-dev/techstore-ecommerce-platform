import { Pipe, PipeTransform } from '@angular/core';
import { InventoryResponse } from './models/inventory.model';

/**
 * Lấy tổng tồn kho của một variantId từ danh sách inventory.
 * Dùng trong template xuất kho dropdown để hiển thị tồn.
 *
 * Cách dùng: {{ inventories | stockOf: variantId }}
 */
@Pipe({ name: 'stockOf', standalone: true, pure: false })
export class StockOfPipe implements PipeTransform {
  transform(inventories: InventoryResponse[], variantId: number): number {
    if (!inventories?.length) return 0;
    return inventories
      .filter(i => i.variantId === variantId && i.status === 'ACTIVE')
      .reduce((sum, i) => sum + i.stock, 0);
  }
}