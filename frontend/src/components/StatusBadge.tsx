import React from 'react';
import { RequisitionStatus, PurchaseOrderStatus, SupplierStatus, StockMovementType } from '../types';

interface StatusBadgeProps {
  status: RequisitionStatus | PurchaseOrderStatus | SupplierStatus | StockMovementType | string;
}

export const StatusBadge: React.FC<StatusBadgeProps> = ({ status }) => {
  const getBadgeClass = (s: string): string => {
    switch (s) {
      case 'APPROVED':
      case 'RECEIVED':
      case 'ACTIVE':
      case 'INBOUND_RECEIPT':
      case 'ADJUSTMENT_ADD':
        return 'badge-success';

      case 'SUBMITTED':
      case 'SENT':
      case 'PARTIALLY_RECEIVED':
        return 'badge-warning';

      case 'REJECTED':
      case 'CANCELLED':
      case 'BLACKLISTED':
      case 'OUTBOUND_DISPATCH':
      case 'ADJUSTMENT_SUBTRACT':
        return 'badge-danger';

      case 'DRAFT':
      case 'INACTIVE':
      case 'INITIAL_STOCK':
      default:
        return 'badge-secondary';
    }
  };

  const formatText = (text: string): string => {
    return text.replace(/_/g, ' ');
  };

  return (
    <span className={`status-badge ${getBadgeClass(status)}`}>
      {formatText(status)}
    </span>
  );
};
