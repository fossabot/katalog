import {Component, OnDestroy, OnInit} from "@angular/core";
import {interval, Subscription} from "rxjs";
import {PlatformLocation} from "@angular/common";
import {HttpClient} from "@angular/common/http";

@Component({
  selector: 'app-starting-up',
  templateUrl: './starting-up.component.html'
})
export class StartingUpComponent implements OnInit, OnDestroy {
  private subscription: Subscription;

  constructor(
    private http: HttpClient,
    private platformLocation: PlatformLocation
  ) {
  }

  ngOnInit(): void {
    this.subscription = interval(1000).subscribe(async () => {
      // This API call should always return something, if Katalog has started
      let katalogVersion = await this.http.get<void>('/api/v1/katalog/version').toPromise();

      if (katalogVersion != null) {
        // Katalog has started! Let's just reload the entire application by navigating to the base url
        window.location.href = this.platformLocation.getBaseHrefFromDOM();
      }
    });
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }
}
