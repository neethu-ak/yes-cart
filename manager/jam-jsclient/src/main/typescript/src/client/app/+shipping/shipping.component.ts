/*
 * Copyright 2009 Inspire-Software.com
 *
 *    Licensed under the Apache License, Version 2.0 (the 'License');
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an 'AS IS' BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { I18nEventBus, ShopEventBus, ShippingService, FulfilmentService, PaymentService, UserEventBus, Util } from './../shared/services/index';
import { ModalComponent, ModalResult, ModalAction } from './../shared/modal/index';
import {
  CarrierVO, CarrierInfoVO, CarrierSlaVO, CarrierSlaInfoVO,
  ShopVO, PaymentGatewayInfoVO, FulfilmentCentreInfoVO,
  Pair, SearchContextVO, SearchResultVO
} from './../shared/model/index';
import { FormValidationEvent, Futures, Future } from './../shared/event/index';
import { Config } from './../shared/config/env.config';
import { LogUtil } from './../shared/log/index';

@Component({
  selector: 'yc-shipping',
  moduleId: module.id,
  templateUrl: 'shipping.component.html',
})

export class ShippingComponent implements OnInit, OnDestroy {

  private static CARRIERS:string = 'carriers';
  private static CARRIER:string = 'carrier';
  private static SLA:string = 'sla';

  private viewMode:string = ShippingComponent.CARRIERS;

  private carriers:SearchResultVO<CarrierInfoVO>;
  private carrierFilter:string;

  private delayedFilteringCarrier:Future;
  private delayedFilteringCarrierMs:number = Config.UI_INPUT_DELAY;

  private selectedCarrier:CarrierVO;

  private carrierEdit:CarrierVO;

  @ViewChild('deleteConfirmationModalDialog')
  private deleteConfirmationModalDialog:ModalComponent;

  private pgs:Array<PaymentGatewayInfoVO> = [];
  private fcs:Array<FulfilmentCentreInfoVO> = [];
  private shops:Array<ShopVO> = [];

  private selectedSla:CarrierSlaInfoVO;

  private slaEdit:CarrierSlaVO;

  private deleteValue:String;

  private shopAllSub:any;

  private loading:boolean = false;

  private changed:boolean = false;
  private validForSave:boolean = false;

  constructor(private _shippingService:ShippingService,
              private _paymentService:PaymentService,
              private _fulfilmentService:FulfilmentService) {
    LogUtil.debug('ShippingComponent constructed');
    this.shopAllSub = ShopEventBus.getShopEventBus().shopsUpdated$.subscribe(shopsevt => {
      this.shops = shopsevt;
    });
    this.carriers = this.newSearchResultCarrierInstance();
  }

  newCarrierInstance():CarrierVO {
    return {
      carrierId: 0,
      code: null,
      name: '',
      description: '',
      displayNames: [],
      displayDescriptions: [],
      carrierShops: [],
      slas: []
    };
  }

  newSearchResultCarrierInstance():SearchResultVO<CarrierInfoVO> {
    return {
      searchContext: {
        parameters: {
          filter: []
        },
        start: 0,
        size: Config.UI_TABLE_PAGE_SIZE,
        sortBy: null,
        sortDesc: false
      },
      items: [],
      total: 0
    };
  }

  newSlaInstance():CarrierSlaVO {
    let carrierId = this.selectedCarrier != null ? this.selectedCarrier.carrierId : 0;
    return {
      carrierslaId: 0, carrierId: carrierId, code: null, name: '', description: '',
      displayNames: [], displayDescriptions: [],
      maxDays: 1, minDays: 1, excludeWeekDays: [], excludeDates: [], guaranteed: false, namedDay: false, excludeCustomerTypes: null, slaType: 'F', script: '',
      billingAddressNotRequired: false, deliveryAddressNotRequired: false,
      supportedPaymentGateways: [], supportedFulfilmentCentres: [],
      externalRef: null
    };
  }

  ngOnInit() {
    LogUtil.debug('ShippingComponent ngOnInit');
    this.onRefreshHandler();
    let that = this;
    this.delayedFilteringCarrier = Futures.perpetual(function() {
      that.getFilteredCarriers();
    }, this.delayedFilteringCarrierMs);
  }

  ngOnDestroy() {
    LogUtil.debug('ShippingComponent ngOnDestroy');
    if (this.shopAllSub) {
      this.shopAllSub.unsubscribe();
    }
  }


  protected onCarrierFilterChange(event:any) {
    this.carriers.searchContext.start = 0; // changing filter means we need to start from first page
    this.delayedFilteringCarrier.delay();
  }

  protected onRefreshHandler() {
    LogUtil.debug('ShippingComponent refresh handler');
    if (UserEventBus.getUserEventBus().current() != null) {
      this.getFilteredCarriers();
    }
  }

  protected onPageSelected(page:number) {
    LogUtil.debug('ShippingComponent onPageSelected', page);
    this.carriers.searchContext.start = page;
    this.delayedFilteringCarrier.delay();
  }

  protected onSortSelected(sort:Pair<string, boolean>) {
    LogUtil.debug('ShippingComponent ononSortSelected', sort);
    if (sort == null) {
      this.carriers.searchContext.sortBy = null;
      this.carriers.searchContext.sortDesc = false;
    } else {
      this.carriers.searchContext.sortBy = sort.first;
      this.carriers.searchContext.sortDesc = sort.second;
    }
    this.delayedFilteringCarrier.delay();
  }

  protected onCarrierSelected(data:CarrierVO) {
    LogUtil.debug('ShippingComponent onCarrierSelected', data);
    this.selectedCarrier = data;
  }

  protected onCarrierChanged(event:FormValidationEvent<CarrierVO>) {
    LogUtil.debug('ShippingComponent onCarrierChanged', event);
    this.changed = true;
    this.validForSave = event.valid;
    this.carrierEdit = event.source;
  }

  protected onSlaSelected(data:CarrierSlaInfoVO) {
    LogUtil.debug('ShippingComponent onSlaSelected', data);
    this.selectedSla = data;
  }

  protected onSlaAdd(data:CarrierSlaInfoVO) {
    LogUtil.debug('ShippingComponent onSlaAdd', data);
    this.onRowNew();
  }

  protected onSlaEdit(data:CarrierSlaInfoVO) {
    LogUtil.debug('ShippingComponent onSlaEdit', data);
    this.onRowEditSla(data);
  }

  protected onSlaDelete(data:CarrierSlaInfoVO) {
    LogUtil.debug('ShippingComponent onSlaDelete', data);
    this.selectedSla = data;
    this.onRowDelete(data);
  }

  protected onSlaChanged(event:FormValidationEvent<CarrierSlaVO>) {
    LogUtil.debug('ShippingComponent onSlaChanged', event);
    this.changed = true;
    this.validForSave = event.valid;
    this.slaEdit = event.source;
  }

  protected onBackToList() {
    LogUtil.debug('ShippingComponent onBackToList handler');
    if (this.viewMode === ShippingComponent.SLA) {
      this.slaEdit = null;
      if (this.carrierEdit != null) {
        this.viewMode = ShippingComponent.CARRIER;
      }
    } else if (this.viewMode === ShippingComponent.CARRIER) {
      this.carrierEdit = null;
      this.slaEdit = null;
      this.selectedSla = null;
      this.viewMode = ShippingComponent.CARRIERS;
    }
  }

  protected onRowNew() {
    LogUtil.debug('ShippingComponent onRowNew handler');
    this.changed = false;
    this.validForSave = false;
    if (this.viewMode === ShippingComponent.CARRIERS) {
      this.carrierEdit = this.newCarrierInstance();
      this.viewMode = ShippingComponent.CARRIER;
    } else if (this.viewMode === ShippingComponent.CARRIER) {
      this.slaEdit = this.newSlaInstance();
      this.viewMode = ShippingComponent.SLA;
    }
  }

  protected onRowDelete(row:any) {
    LogUtil.debug('ShippingComponent onRowDelete handler', row);
    this.deleteValue = row.name;
    this.deleteConfirmationModalDialog.show();
  }

  protected onRowDeleteSelected() {
    if (this.selectedSla != null) {
      this.onRowDelete(this.selectedSla);
    } else if (this.selectedCarrier != null) {
      this.onRowDelete(this.selectedCarrier);
    }
  }


  protected onRowEditCarrier(row:CarrierInfoVO) {
    LogUtil.debug('ShippingComponent onRowEditCarrier handler', row);
    this.loading = true;
    let _sub:any = this._shippingService.getCarrierById(row.carrierId).subscribe(res => {
      LogUtil.debug('ShippingComponent getCarrierById', res);
      this.carrierEdit = res;
      this.changed = false;
      this.validForSave = false;
      this.viewMode = ShippingComponent.CARRIER;
      this.loading = false;
      _sub.unsubscribe();
    });
  }

  protected onRowEditSla(row:CarrierSlaInfoVO) {
    LogUtil.debug('ShippingComponent onRowEditSla handler', row);
    this.slaEdit = Util.clone(row);
    this.changed = false;
    this.validForSave = false;
    this.viewMode = ShippingComponent.SLA;
  }

  protected onRowEditSelected() {
    if (this.selectedSla != null) {
      this.onRowEditSla(this.selectedSla);
    } else if (this.selectedCarrier != null) {
      this.onRowEditCarrier(this.selectedCarrier);
    }
  }


  protected onSaveHandler() {

    if (this.validForSave && this.changed) {

     if (this.slaEdit != null) {

        LogUtil.debug('ShippingComponent Save handler sla', this.slaEdit);

        this.loading = true;
        let _sub:any = this._shippingService.saveCarrierSla(this.slaEdit).subscribe(
            rez => {
              let pk = this.slaEdit.carrierslaId;
              if (pk > 0) {
                LogUtil.debug('ShippingComponent sla edit', rez);
                if (this.carrierEdit != null) {
                  let idx = this.carrierEdit.slas.findIndex(rez => rez.carrierslaId == pk);
                  if (idx !== -1) {
                    this.carrierEdit.slas[idx] = rez;
                    this.carrierEdit.slas = this.carrierEdit.slas.slice(0, this.carrierEdit.slas.length); // reset to propagate changes
                  }
                }
              } else {
                if (this.carrierEdit != null) {
                  this.carrierEdit.slas.push(rez);
                  this.carrierEdit.slas = this.carrierEdit.slas.slice(0, this.carrierEdit.slas.length); // reset to propagate changes
                }
                LogUtil.debug('ShippingComponent sla added', rez);
              }
              this.changed = false;
              this.selectedSla = rez;
              this.slaEdit = null;
              this.loading = false;
              this.viewMode = ShippingComponent.CARRIER;
              _sub.unsubscribe();
          }
        );
      } else if (this.carrierEdit != null) {

        LogUtil.debug('ShippingComponent Save handler carrier', this.carrierEdit);

       this.loading = true;
        let _sub:any = this._shippingService.saveCarrier(this.carrierEdit).subscribe(
            rez => {
              LogUtil.debug('ShippingComponent country changed', rez);
              this.changed = false;
              this.selectedCarrier = rez;
              this.selectedSla = null;
              this.carrierEdit = null;
              this.loading = false;
              _sub.unsubscribe();
              this.getFilteredCarriers();
          }
        );
      }

    }

  }

  protected onDiscardEventHandler() {
    LogUtil.debug('ShippingComponent discard handler');
    if (this.viewMode === ShippingComponent.SLA) {
      if (this.selectedSla != null) {
        this.onRowEditSla(this.selectedSla);
      } else {
        this.onRowNew();
      }
    }
    if (this.viewMode === ShippingComponent.CARRIER) {
      if (this.selectedCarrier != null) {
        this.onRowEditCarrier(this.selectedCarrier);
      } else {
        this.onRowNew();
      }
    }
  }

  protected onDeleteConfirmationResult(modalresult: ModalResult) {
    LogUtil.debug('ShippingComponent onDeleteConfirmationResult modal result is ', modalresult);
    if (ModalAction.POSITIVE === modalresult.action) {

     if (this.selectedSla != null) {
        LogUtil.debug('ShippingComponent onDeleteConfirmationResult', this.selectedSla);

       this.loading = true;
        let _sub:any = this._shippingService.removeCarrierSla(this.selectedSla).subscribe(res => {
          LogUtil.debug('ShippingComponent removeSla', this.selectedSla);
          let pk = this.selectedSla.carrierslaId;
          this.slaEdit = null;
          if (this.carrierEdit != null) {
            let idx2 = this.carrierEdit.slas.findIndex(rez => rez.carrierslaId == pk);
            if (idx2 !== -1) {
              this.carrierEdit.slas.splice(idx2, 1);
              this.carrierEdit.slas = this.carrierEdit.slas.slice(0, this.carrierEdit.slas.length); // reset to propagate changes
            }
          }
          this.selectedSla = null;
          this.loading = false;
          _sub.unsubscribe();
          this.viewMode = ShippingComponent.CARRIER;
        });

      } else if (this.selectedCarrier != null) {
        LogUtil.debug('ShippingComponent onDeleteConfirmationResult', this.selectedCarrier);

        this.loading = true;
        let _sub:any = this._shippingService.removeCarrier(this.selectedCarrier).subscribe(res => {
          LogUtil.debug('ShippingComponent removeCarrier', this.selectedCarrier);
          this.selectedCarrier = null;
          this.carrierEdit = null;
          this.loading = false;
          _sub.unsubscribe();
          this.getFilteredCarriers();
        });
      }
    }
  }

  protected onClearFilterCarrier() {

    this.carrierFilter = '';
    this.getFilteredCarriers();

  }

  private getFilteredCarriers() {

    LogUtil.debug('ShippingComponent getFilteredCarriers');

    this.loading = true;

    this.carriers.searchContext.parameters.filter = [ this.carrierFilter ];
    this.carriers.searchContext.size = Config.UI_TABLE_PAGE_SIZE;

    let _sub:any = this._shippingService.getFilteredCarriers(this.carriers.searchContext).subscribe( allcarriers => {
      LogUtil.debug('ShippingComponent getFilteredCarriers', allcarriers);
      this.carriers = allcarriers;
      this.selectedCarrier = null;
      this.carrierEdit = null;
      this.viewMode = ShippingComponent.CARRIERS;
      this.changed = false;
      this.validForSave = false;
      this.loading = false;
      _sub.unsubscribe();

      if (this.pgs == null || this.pgs.length == 0) {
        let lang = I18nEventBus.getI18nEventBus().current();
        let _sub2: any = this._paymentService.getPaymentGateways(lang).subscribe(allpgs => {
          LogUtil.debug('ShippingComponent getPaymentGateways', allpgs);
          this.pgs = allpgs;
          _sub2.unsubscribe();
        });
      }
      if (this.fcs.length == 0) {
        let _ctx: SearchContextVO = {
          parameters: {
            filter: []
          },
          start: 0,
          size: 1000,
          sortBy: null,
          sortDesc: false
        };
        let _sub3: any = this._fulfilmentService.getFilteredFulfilmentCentres(_ctx).subscribe(allfcs => {
          LogUtil.debug('ShippingComponent getAllFulfilmentCentres', allfcs);
          this.fcs = allfcs != null ? allfcs.items : [];
          _sub3.unsubscribe();
        });
      }
    });
  }

}
