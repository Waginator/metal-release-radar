import {SpotifyRestClient} from "../clients/spotify-rest-client";
import {ToastService} from "./toast-service";
import {UrlService} from "./url-service";
import {LoadingIndicatorService} from "./loading-indicator-service";
import {SpotifyArtist} from "../model/spotify-artist.model";
import {AlertService} from "./alert-service";
import {Endpoints} from "../config/endpoints";

export class SpotifySynchronizationRenderService {

    private static readonly BUTTON_BAR_DIV_NAME = "button-bar";
    private static readonly ARTISTS_SELECTION_BAR_ID = "artists-selection-bar";
    private static readonly ARTISTS_CONTAINER_ID = "artists-container";

    private readonly spotifyRestClient: SpotifyRestClient;
    private readonly loadingIndicatorService: LoadingIndicatorService;
    private readonly toastService: ToastService;
    private readonly urlService: UrlService;
    private readonly alertService: AlertService;

    private readonly artistSelectionElement: HTMLDivElement;
    private readonly artistContainerElement: HTMLDivElement;

    private readonly connectWithSpotifyButton: HTMLButtonElement;
    private readonly fetchArtistsButton: HTMLButtonElement;
    private readonly synchronizeArtistsButton: HTMLButtonElement;
    private readonly disconnectSpotifyButton: HTMLButtonElement;

    constructor(spotifyRestClient: SpotifyRestClient, loadingIndicatorService: LoadingIndicatorService,
      toastService: ToastService, urlService: UrlService, alertService: AlertService) {
        this.spotifyRestClient = spotifyRestClient;
        this.loadingIndicatorService = loadingIndicatorService;
        this.toastService = toastService;
        this.urlService = urlService;
        this.alertService = alertService;

        this.connectWithSpotifyButton = document.getElementById("connect-with-spotify-button") as HTMLButtonElement;
        this.disconnectSpotifyButton = document.getElementById("disconnect-spotify-button") as HTMLButtonElement;
        this.fetchArtistsButton = document.getElementById("fetch-artists-button") as HTMLButtonElement;
        this.synchronizeArtistsButton = document.getElementById("synchronize-artists-button") as HTMLButtonElement;
        this.artistSelectionElement = document.getElementById(SpotifySynchronizationRenderService.ARTISTS_SELECTION_BAR_ID) as HTMLDivElement;
        this.artistContainerElement = document.getElementById(SpotifySynchronizationRenderService.ARTISTS_CONTAINER_ID) as HTMLDivElement;

        this.addEventListener();
    }

    private addEventListener(): void {
        this.connectWithSpotifyButton.addEventListener("click", this.onConnectWithSpotifyClicked.bind(this));
        this.disconnectSpotifyButton.addEventListener("click", this.onDisconnectSpotifyClicked.bind(this));
        this.synchronizeArtistsButton.addEventListener("click", this.onSynchronizeArtistsClicked.bind(this));
        document.getElementById("fetch-from-saved-albums")!.addEventListener("click", this.onFetchSpotifyArtistsFromAlbumsClicked.bind(this));
        document.getElementById("fetch-from-saved-artists")!.addEventListener("click", this.onFetchSpotifyArtistsFromArtistsClicked.bind(this));
        document.getElementById("fetch-from-both")!.addEventListener("click", this.onFetchSpotifyArtistsFromBothClicked.bind(this));
        document.getElementById("select-all-link")!.addEventListener("click", this.onSelectOrDeselectAllArtistsClicked.bind(this, true));
        document.getElementById("deselect-all-link")!.addEventListener("click", this.onSelectOrDeselectAllArtistsClicked.bind(this, false));
    }

    public init(): void {
        const path = this.urlService.getPathFromUrl();
        if (path.endsWith(Endpoints.SPOTIFY_CALLBACK)) {
            this.loadingIndicatorService.showLoadingIndicator(SpotifySynchronizationRenderService.BUTTON_BAR_DIV_NAME);
            const state = this.urlService.getParameterFromUrl("state")
            const code = this.urlService.getParameterFromUrl("code")
            this.spotifyRestClient.fetchInitialToken(state, code)
              .then(() => this.toastService.createInfoToast("Successfully connected with Spotify!"))
              .then(() => window.location.href = Endpoints.SPOTIFY_SYNCHRONIZATION)
        }
        else {
            this.initButtonBar();
        }
    }

    private initButtonBar(): void {
        const response = this.spotifyRestClient.existsAuthorization();
        response.then(response => {
            if (response.exists) {
                document.getElementById("button-bar")!.removeChild(this.connectWithSpotifyButton);
                [this.disconnectSpotifyButton, this.fetchArtistsButton, this.synchronizeArtistsButton].forEach(button => {
                    button.classList.remove("invisible");
                });
            }
        });
    }

    private onConnectWithSpotifyClicked(): void {
        const authorizationResponse = this.spotifyRestClient.createAuthorizationUrl()
        authorizationResponse.then(response => window.location.href = response.authorizationUrl);
    }

    private onDisconnectSpotifyClicked(): void {
        this.spotifyRestClient.disconnectSpotifyAccount().then(() => {
            this.toastService.createInfoToast("Spotify account successfully disconnected");
            this.deactivateButtonBar();
          }
        )
    }

    private deactivateButtonBar(): void {
        document.getElementById("button-bar")!.replaceChild(this.connectWithSpotifyButton, this.disconnectSpotifyButton);
        [this.disconnectSpotifyButton, this.fetchArtistsButton, this.synchronizeArtistsButton].forEach(button => {
            button.classList.add("invisible");
        });
    }

    private onFetchSpotifyArtistsFromAlbumsClicked(): void {
        this.fetchArtists(["ALBUMS"])
    }

    private onFetchSpotifyArtistsFromArtistsClicked(): void {
        this.fetchArtists(["ARTISTS"])
    }

    private onFetchSpotifyArtistsFromBothClicked(): void {
        this.fetchArtists(["ALBUMS", "ARTISTS"])
    }

    private fetchArtists(fetchTypes: string[]): void {
        this.loadingIndicatorService.showLoadingIndicator(SpotifySynchronizationRenderService.ARTISTS_CONTAINER_ID);
        this.clearArtistsContainer();
        const savedArtists = this.spotifyRestClient.fetchSavedArtists(fetchTypes);
        let savedArtistsAvailable = false;
        savedArtists.then(response => {
            savedArtistsAvailable = response.artists.length > 0;
            response.artists.forEach(artist => {
                const artistTemplateElement = document.getElementById("artist-card")! as HTMLTemplateElement;
                const artistTemplateNode = document.importNode(artistTemplateElement.content, true);
                const artistDivElement = artistTemplateNode.firstElementChild as HTMLDivElement;
                const artistThumbElement = artistDivElement.querySelector("#thumb") as HTMLImageElement;
                const artistNameElement = artistDivElement.querySelector("#artist-name") as HTMLParagraphElement;
                const artistInfoElement = artistDivElement.querySelector("#artist-info") as HTMLParagraphElement;

                artistDivElement.id = artist.id;
                artistThumbElement.src = artist.imageUrl;
                artistNameElement.textContent = artist.name;
                artistInfoElement.innerHTML = this.buildArtistInfoText(artist);
                artistDivElement.addEventListener("click", this.onArtistClicked.bind(this, artistDivElement));
                this.artistContainerElement.insertAdjacentElement("beforeend", artistDivElement);
            });
        }).finally(() => {
            if (savedArtistsAvailable) {
                this.artistSelectionElement.classList.remove("invisible");
                this.synchronizeArtistsButton?.classList.remove("disabled");
            }
            else {
                const infoIcon = '<span class="material-icons">info</span>';
                const infoMessage = `${infoIcon} You already follow all the artists on Metal Detector that you also follow on Spotify.`;
                const infoMessageElement = this.alertService.renderInfoAlert(infoMessage, true);
                this.artistContainerElement.insertAdjacentElement("beforeend", infoMessageElement);
            }
            this.loadingIndicatorService.hideLoadingIndicator(SpotifySynchronizationRenderService.ARTISTS_CONTAINER_ID);
        });
    }

    private buildArtistInfoText(artist: SpotifyArtist): string {
        const followerCount = new Intl.NumberFormat("en-us", {minimumFractionDigits: 0}).format(artist.follower);
        const follower = `${followerCount} followers on Spotify`;
        const genres = artist.genres.slice(0, 3).join(", ");
        return `${genres}<br />${follower}`;
    }

    private onSynchronizeArtistsClicked(): void {
        const disabled = this.synchronizeArtistsButton.classList.contains("disabled");
        if (!disabled) {
            this.synchronizeArtists();
        }
    }

    private synchronizeArtists(): void {
        const artistCards = this.artistContainerElement.getElementsByClassName("spotify-synchro-card");
        const selectedArtistIds: string[] = [];
        Array.from(artistCards).forEach(artistCard => {
            const artistCheckbox = artistCard.querySelector("#artist-check-box") as HTMLSpanElement;
            const isSelected = artistCheckbox.innerText === "check_box";
            if (isSelected) {
                selectedArtistIds.push(artistCard.id);
            }
        });

        this.clearArtistsContainer();
        this.loadingIndicatorService.showLoadingIndicator(SpotifySynchronizationRenderService.ARTISTS_CONTAINER_ID);
        this.spotifyRestClient.synchronizeArtists(selectedArtistIds).then(response => {
            const successIcon = '<span class="material-icons">check_circle</span>';
            const successMessage = `${successIcon} You are now following ${response.artistsCount} new artists on Metal Detector and ` +
                                   `will be notified as soon as these artists announce a new release.`;
            const successMessageElement = this.alertService.renderSuccessAlert(successMessage, true);
            this.artistContainerElement.insertAdjacentElement("beforeend", successMessageElement);
        }).finally(() => {
            this.loadingIndicatorService.hideLoadingIndicator(SpotifySynchronizationRenderService.ARTISTS_CONTAINER_ID);
            this.artistSelectionElement.classList.add("invisible");
            this.synchronizeArtistsButton?.classList.add("disabled");
        });
    }

    private onArtistClicked(artistDivElement: HTMLDivElement): void {
        const artistCheckbox = artistDivElement.querySelector("#artist-check-box") as HTMLSpanElement;
        this.handleSelection(artistDivElement, artistCheckbox, artistCheckbox.innerText !== "check_box");
    }

    private onSelectOrDeselectAllArtistsClicked(shouldSelect: boolean): void {
        const artistCards = this.artistContainerElement.getElementsByClassName("spotify-synchro-card");
        Array.from(artistCards).forEach(artistCard => {
            const artistCheckbox = artistCard.querySelector("#artist-check-box") as HTMLSpanElement;
            this.handleSelection(artistCard as HTMLDivElement, artistCheckbox, shouldSelect);
        });
    }

    private handleSelection(artistDivElement: HTMLDivElement, artistCheckbox: HTMLSpanElement, shouldSelect: boolean): void {
        const artistThumbElement = artistDivElement.querySelector("#thumb") as HTMLImageElement;
        const artistNameElement = artistDivElement.querySelector("#artist-name") as HTMLParagraphElement;
        const artistInfoElement = artistDivElement.querySelector("#artist-info") as HTMLParagraphElement;

        artistCheckbox.innerText = shouldSelect ? "check_box" : "check_box_outline_blank";
        shouldSelect ? artistCheckbox.classList.add("md-success") : artistCheckbox.classList.remove("md-success");
        shouldSelect ? artistThumbElement.classList.remove("img-inactive") : artistThumbElement.classList.add("img-inactive");
        shouldSelect ? artistNameElement.classList.remove("text-muted") : artistNameElement.classList.add("text-muted");
        shouldSelect ? artistInfoElement.classList.remove("text-muted") : artistInfoElement.classList.add("text-muted");
    }

    private clearArtistsContainer(): void {
        this.artistContainerElement.innerHTML = "";
    }
}
