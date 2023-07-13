import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { SharedModule } from 'src/app/shared/shared.module';
import { StatisticsRoutingModule } from './statistics-routing.module';
import { StatisticsTileComponent } from './statistics-tile/statistics-tile.component';
import { StatisticsComponent } from './statistics/statistics.component';

@NgModule({
    declarations: [StatisticsComponent, StatisticsTileComponent],
    imports: [CommonModule, SharedModule, StatisticsRoutingModule],
})
export class StatisticsModule {}
